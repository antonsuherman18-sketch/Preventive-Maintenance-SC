package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.random.Random

/**
 * Utilitas kompresi foto untuk memastikan hasil foto maksimal 50 KB (51.200 bytes).
 */
object ImageCompressor {
    const val MAX_SIZE_BYTES = 50 * 1024L // Maksimal 50 KB

    data class CompressionResult(
        val filePath: String,
        val sizeBytes: Long,
        val sizeKb: String
    )

    /**
     * Membuat Uri sementara untuk menangkap foto dari kamera menggunakan FileProvider.
     */
    fun createCameraTempUri(context: Context): Uri {
        val photosDir = File(context.cacheDir, "camera_captures").apply {
            if (!exists()) mkdirs()
        }
        val tempFile = File(photosDir, "capture_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Mengompresi gambar dari Uri sehingga ukuran file maksimal 50 KB.
     * Mengembalikan CompressionResult berisi path file hasil kompresi dan ukurannya.
     */
    fun compressImage(context: Context, sourceUri: Uri): CompressionResult? {
        return try {
            val resolver = context.contentResolver

            // 1. Cek rotasi EXIF dari gambar asli
            var rotation = 0
            try {
                resolver.openInputStream(sourceUri)?.use { input ->
                    val exif = ExifInterface(input)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (_: Exception) {}

            // 2. Decode bounds terlebih dahulu untuk menghitung inSampleSize
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            var inSample = 1
            var origW = boundsOptions.outWidth
            var origH = boundsOptions.outHeight
            val targetInitialDim = 900
            while ((origW / (inSample * 2)) >= targetInitialDim || (origH / (inSample * 2)) >= targetInitialDim) {
                inSample *= 2
            }

            // 3. Decode bitmap dengan inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSample
                inPreferredConfig = Bitmap.Config.RGB_565 // Menghemat memori RAM
            }
            var bitmap: Bitmap? = resolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            // Terapkan rotasi jika diperlukan
            if (rotation != 0 && bitmap != null) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }

            val currentBitmap = bitmap ?: return null
            var activeBitmap: Bitmap = currentBitmap

            // 4. Downscale jika dimensi melebihi 720px
            val maxDimension = 720
            if (activeBitmap.width > maxDimension || activeBitmap.height > maxDimension) {
                val scale = minOf(maxDimension.toFloat() / activeBitmap.width, maxDimension.toFloat() / activeBitmap.height)
                val newW = (activeBitmap.width * scale).toInt().coerceAtLeast(1)
                val newH = (activeBitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(activeBitmap, newW, newH, true)
                if (scaled != activeBitmap) {
                    activeBitmap.recycle()
                    activeBitmap = scaled
                }
            }

            // 5. Kompresi JPEG dengan kualitas bertahap
            var quality = 85
            var outputStream = ByteArrayOutputStream()
            activeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            while (outputStream.size() > MAX_SIZE_BYTES && quality > 15) {
                outputStream.reset()
                quality -= 15
                activeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            // Jika masih di atas 50 KB, kurangi dimensi gambar secara progresif
            while (outputStream.size() > MAX_SIZE_BYTES && activeBitmap.width > 200) {
                outputStream.reset()
                val smallerW = (activeBitmap.width * 0.75).toInt().coerceAtLeast(100)
                val smallerH = (activeBitmap.height * 0.75).toInt().coerceAtLeast(100)
                val scaled = Bitmap.createScaledBitmap(activeBitmap, smallerW, smallerH, true)
                if (scaled != activeBitmap) {
                    activeBitmap.recycle()
                    activeBitmap = scaled
                }
                quality = 70
                activeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                while (outputStream.size() > MAX_SIZE_BYTES && quality > 15) {
                    outputStream.reset()
                    quality -= 15
                    activeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                }
            }

            val byteArray = outputStream.toByteArray()
            outputStream.close()
            activeBitmap.recycle()

            // 6. Simpan hasil kompresi ke direktori internal aplikasi agar awet & persisten
            val photosDir = File(context.filesDir, "abnormality_photos").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(photosDir, "abnormality_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}.jpg")
            FileOutputStream(destinationFile).use { fos ->
                fos.write(byteArray)
                fos.flush()
            }

            val sizeKb = String.format(Locale.US, "%.1f", destinationFile.length() / 1024.0)
            CompressionResult(
                filePath = destinationFile.absolutePath,
                sizeBytes = destinationFile.length(),
                sizeKb = sizeKb
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mengambil info ukuran file dalam KB untuk file path atau data base64 tertentu.
     */
    fun getFileSizeKb(filePath: String?): String {
        if (filePath.isNullOrBlank()) return "0 KB"
        val file = File(filePath)
        if (file.exists() && file.isFile) {
            return String.format(Locale.US, "%.1f KB", file.length() / 1024.0)
        }
        // Jika format string Base64 atau data URI
        if (filePath.startsWith("data:image") || filePath.length > 200) {
            try {
                val clean = if (filePath.contains(",")) filePath.substringAfter(",") else filePath
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                if (bytes != null && bytes.isNotEmpty()) {
                    return String.format(Locale.US, "%.1f KB", bytes.size / 1024.0)
                }
            } catch (_: Exception) {}
        }
        return "0 KB"
    }

    /**
     * Mendapatkan model gambar untuk Coil AsyncImage (File, Uri, atau ByteArray).
     * Mendukung pemuatan baik dari file lokal maupun byte array gambar yang disinkronkan.
     */
    fun getPhotoModel(pathOrData: String?): Any? {
        if (pathOrData.isNullOrBlank()) return null
        val file = File(pathOrData)
        if (file.exists() && file.isFile && file.length() > 0) {
            return file
        }
        if (pathOrData.startsWith("content://") || pathOrData.startsWith("file://")) {
            return Uri.parse(pathOrData)
        }
        // Jika berbentuk Base64 string
        if (pathOrData.startsWith("data:image") || pathOrData.length > 200) {
            try {
                val clean = if (pathOrData.contains(",")) pathOrData.substringAfter(",") else pathOrData
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                if (bytes != null && bytes.isNotEmpty()) {
                    return bytes
                }
            } catch (_: Exception) {}
        }
        return file // Kembalikan File fallback agar Coil tetap memproses jika file baru saja dibuat
    }

    /**
     * Mengonversi file gambar lokal menjadi string Base64 untuk disinkronkan ke Cloud Firestore.
     */
    fun fileToBase64(filePath: String?): String? {
        if (filePath.isNullOrBlank()) return null
        return try {
            val file = File(filePath)
            if (file.exists() && file.isFile && file.length() > 0) {
                val bytes = file.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Menyimpan data string Base64 dari Cloud Firestore ke penyimpanan internal lokal perangkat (HP).
     * Memastikan foto dapat dibuka dan dilihat di HP mana pun secara offline maupun online.
     */
    fun saveBase64ToFile(context: Context?, base64Data: String?, fileName: String): String? {
        if (base64Data.isNullOrBlank()) return null
        return try {
            val clean = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            if (bytes == null || bytes.isEmpty()) return null

            val baseDir = context?.filesDir ?: File("/data/data/com.example/files")
            val photosDir = File(baseDir, "abnormality_photos").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(photosDir, fileName)
            // Tulis file jika belum ada atau ukurannya berbeda
            if (!targetFile.exists() || targetFile.length() != bytes.size.toLong()) {
                FileOutputStream(targetFile).use { fos ->
                    fos.write(bytes)
                    fos.flush()
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
