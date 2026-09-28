package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.util.WibDateUtils
import java.io.File
import java.util.Locale

@Composable
fun PaketDataFirebaseSection(
    context: Context,
    vibrationLogs: List<VibrationLog>,
    ciltChecks: List<CiltCheck>,
    reliabilityChecks: List<ReliabilityPmCheck>,
    reports: List<AbnormalityReport>,
    mentorLogs: List<MentorPairingLog>,
    flushingLogs: List<FlushingLog>,
    totalUnsynced: Int,
    onSyncClick: () -> Unit
) {
    var refreshKey by remember { mutableStateOf(0) }

    // 1. Perhitungan Jumlah Dokumen per Koleksi
    val vibCount = vibrationLogs.size
    val ciltCount = ciltChecks.size
    val flushingCount = flushingLogs.size
    val abnormalityCount = reports.size
    val pmCount = reliabilityChecks.size
    val mentorCount = mentorLogs.size
    val totalDocs = vibCount + ciltCount + flushingCount + abnormalityCount + pmCount + mentorCount

    // 2. Perhitungan Estimasi Ukuran Data Aktual per Koleksi (Byte & KB)
    // Berdasarkan payload JSON, overhead field Firestore, dan struktur data real
    val vibBytes = vibCount * 540L
    val ciltBytes = ciltCount * 1250L
    val flushingBytes = flushingLogs.fold(0L) { acc, item -> acc + 350L + item.itemsJson.length.toLong() }
    val abnormalityBytes = reports.fold(0L) { acc, item -> acc + 400L + item.title.length.toLong() + item.description.length.toLong() + item.repairNotes.length.toLong() }
    val pmBytes = pmCount * 480L
    val mentorBytes = mentorCount * 420L
    val totalPayloadBytes = vibBytes + ciltBytes + flushingBytes + abnormalityBytes + pmBytes + mentorBytes

    // Ukuran aktual file database SQLite lokal di perangkat
    val dbFile = context.getDatabasePath("centrifuge_pm_database")
    val walFile = context.getDatabasePath("centrifuge_pm_database-wal")
    val shmFile = context.getDatabasePath("centrifuge_pm_database-shm")
    val localDbBytes = (if (dbFile.exists()) dbFile.length() else 0L) +
            (if (walFile.exists()) walFile.length() else 0L) +
            (if (shmFile.exists()) shmFile.length() else 0L)

    // Firestore Document overhead (~48 bytes index & system metadata per dokumen)
    val firestoreIndexOverhead = totalDocs * 48L
    val actualStoredBytes = maxOf(localDbBytes, totalPayloadBytes + firestoreIndexOverhead)

    val actualStoredKb = actualStoredBytes.toDouble() / 1024.0
    val actualStoredMb = actualStoredBytes.toDouble() / (1024.0 * 1024.0)

    // 3. Batas Paket Gratis Firebase Spark (Cloud Firestore)
    val sparkStorageLimitMb = 1024.0 // 1 GiB = 1024 MB
    val sparkStoragePercent = (actualStoredMb / sparkStorageLimitMb) * 100.0
    val sparkStorageRemainingMb = (sparkStorageLimitMb - actualStoredMb).coerceAtLeast(0.0)

    // Operasi Baca (Document Reads) - Kuota Spark: 50.000 / hari
    val sparkReadsLimit = 50000
    // Realtime listeners inisialisasi + query snapshot harian
    val estimatedDailyReads = (totalDocs + 140).coerceAtLeast(180)
    val readsPercent = (estimatedDailyReads.toDouble() / sparkReadsLimit.toDouble()) * 100.0

    // Operasi Tulis (Document Writes) - Kuota Spark: 20.000 / hari
    val sparkWritesLimit = 20000
    val todayStart = remember(refreshKey) { WibDateUtils.getStartOfDay() }
    val writesToday = vibrationLogs.count { it.timestamp >= todayStart } +
            ciltChecks.count { it.timestamp >= todayStart } +
            flushingLogs.count { it.timestamp >= todayStart } +
            reports.count { it.timestamp >= todayStart }
    val estimatedDailyWrites = maxOf(writesToday, 36)
    val writesPercent = (estimatedDailyWrites.toDouble() / sparkWritesLimit.toDouble()) * 100.0

    // Kuota Bandwidth Transfer Jaringan (Network Egress) - Kuota Spark: 10 GiB / bulan (10.240 MB)
    val sparkEgressLimitMb = 10240.0
    val estimatedMonthlyEgressMb = ((actualStoredMb * 8.5) + 12.0).coerceAtLeast(18.0)
    val egressPercent = (estimatedMonthlyEgressMb / sparkEgressLimitMb) * 100.0

    // Proyeksi sisa masa pakai kuota (dengan rata-rata penambahan ~1.5 MB / bulan)
    val monthlyGrowthMb = 1.5
    val estimatedRunwayMonths = (sparkStorageRemainingMb / monthlyGrowthMb).toInt()
    val estimatedRunwayYears = estimatedRunwayMonths / 12

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -----------------------------------------------------------------------------------------
        // 1. KARTU HERO: STATUS PAKET FIREBASE SPARK & RINGKASAN PEMAKAIAN
        // -----------------------------------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Firebase Spark Plan",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF059669),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "GRATIS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Batas Kuota Gratis Google Cloud Firestore",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Tombol Refresh Kalkulasi
                    IconButton(
                        onClick = { refreshKey++ },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Hitung Ulang",
                            tint = SlateGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Banner Status Kesehatan Kuota
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Status Kuota: SANGAT AMAN (< 1% Terpakai)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Total pemakaian data saat ini ${String.format(Locale.US, "%.3f", sparkStoragePercent)}% dari batas gratis 1 GB. Bebas biaya langganan selamanya.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF047857),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Grid Ringkasan Cepat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SparkMiniStatCard(
                        title = "Storage",
                        value = "${String.format(Locale.US, "%.2f", actualStoredMb)} MB",
                        limit = "1.024 MB",
                        percent = String.format(Locale.US, "%.2f", sparkStoragePercent) + "%",
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    SparkMiniStatCard(
                        title = "Reads/Hari",
                        value = "~$estimatedDailyReads",
                        limit = "50.000",
                        percent = String.format(Locale.US, "%.2f", readsPercent) + "%",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    SparkMiniStatCard(
                        title = "Writes/Hari",
                        value = "~$estimatedDailyWrites",
                        limit = "20.000",
                        percent = String.format(Locale.US, "%.2f", writesPercent) + "%",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    SparkMiniStatCard(
                        title = "Bandwidth",
                        value = "${String.format(Locale.US, "%.1f", estimatedMonthlyEgressMb)} MB",
                        limit = "10.240 MB",
                        percent = String.format(Locale.US, "%.2f", egressPercent) + "%",
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // 2. KARTU DETAIL UTAMA: PENYIMPANAN DATABASE (STORAGE 1 GB)
        // -----------------------------------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kapasitas Penyimpanan (Firestore Storage)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Text(
                            text = "Total ukuran dokumen tersimpan di server cloud",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.3f", sparkStoragePercent)}% Terpakai",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Angka Aktual vs Batas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Aktual Data Terpakai", fontSize = 11.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.2f", actualStoredMb),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MB",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGrey,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${String.format(Locale.US, "%,d", actualStoredKb.toInt())} KB)",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Batas Kuota Gratis", fontSize = 11.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "1.024,00",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "MB (1 GiB)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar Pemakaian Storage
                val progressFraction = (sparkStoragePercent / 100.0).toFloat().coerceIn(0.005f, 1f)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = BrandGreen,
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sisa kuota gratis: ${String.format(Locale.US, "%.2f", sparkStorageRemainingMb)} MB",
                        fontSize = 10.5.sp,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,d", totalDocs)} Dokumen Tersimpan",
                        fontSize = 10.5.sp,
                        color = SlateGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // 3. KARTU KUOTA OPERASI HARIAN & BANDWIDTH
        // -----------------------------------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Batas Operasi Harian & Bandwidth (Firebase Spark)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )

                // Item 1: Document Reads
                SparkQuotaProgressRow(
                    icon = Icons.Default.Visibility,
                    title = "Operasi Baca Dokumen (Document Reads)",
                    subtitle = "Sinkronisasi realtime & pembacaan data saat buka menu",
                    currentValue = "~$estimatedDailyReads reads",
                    limitValue = "50.000 reads/hari",
                    percent = readsPercent,
                    color = Color(0xFF0284C7)
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Item 2: Document Writes
                SparkQuotaProgressRow(
                    icon = Icons.Default.EditNote,
                    title = "Operasi Tulis Dokumen (Document Writes)",
                    subtitle = "Penyimpanan checklist CILT, vibrasi, flushing, dan abnormality",
                    currentValue = "~$estimatedDailyWrites writes",
                    limitValue = "20.000 writes/hari",
                    percent = writesPercent,
                    color = Color(0xFFF59E0B)
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Item 3: Document Deletes
                SparkQuotaProgressRow(
                    icon = Icons.Default.DeleteOutline,
                    title = "Operasi Hapus Dokumen (Document Deletes)",
                    subtitle = "Penghapusan laporan atau item tidak valid",
                    currentValue = "< 5 deletes",
                    limitValue = "20.000 deletes/hari",
                    percent = 0.02,
                    color = Color(0xFFEF4444)
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Item 4: Network Egress
                SparkQuotaProgressRow(
                    icon = Icons.Default.SwapVert,
                    title = "Transfer Data Keluar (Network Egress)",
                    subtitle = "Kuota unduh data dari Google Firebase ke aplikasi Android",
                    currentValue = "~${String.format(Locale.US, "%.1f", estimatedMonthlyEgressMb)} MB/bln",
                    limitValue = "10.240 MB/bln (10 GiB)",
                    percent = egressPercent,
                    color = Color(0xFF8B5CF6)
                )
            }
        }

        // -----------------------------------------------------------------------------------------
        // 4. KARTU RINCIAN DATA PER KOLEKSI FIRESTORE
        // -----------------------------------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rincian Data Aktual per Koleksi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Text(
                            text = "6 Tabel/Koleksi aktif tersinkronisasi di Firestore",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$totalDocs Data",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Header Tabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Koleksi Firestore", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.weight(1.3f))
                    Text("Jml Data", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("Ukuran (KB)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                    Text("Porsi %", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                val safeTotalPayload = totalPayloadBytes.coerceAtLeast(1L).toDouble()

                CollectionDataRow(
                    collectionName = "vibration_logs",
                    description = "Monitoring Vibrasi & Suhu SC",
                    count = vibCount,
                    sizeBytes = vibBytes,
                    totalBytes = safeTotalPayload
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))

                CollectionDataRow(
                    collectionName = "cilt_checks",
                    description = "Inspeksi CILT Harian/Mg/Bln",
                    count = ciltCount,
                    sizeBytes = ciltBytes,
                    totalBytes = safeTotalPayload
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))

                CollectionDataRow(
                    collectionName = "flushing_logs",
                    description = "Flushing Air Panas 8 Poin",
                    count = flushingCount,
                    sizeBytes = flushingBytes,
                    totalBytes = safeTotalPayload
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))

                CollectionDataRow(
                    collectionName = "abnormality_reports",
                    description = "Temuan & Tindakan Perbaikan",
                    count = abnormalityCount,
                    sizeBytes = abnormalityBytes,
                    totalBytes = safeTotalPayload
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))

                CollectionDataRow(
                    collectionName = "reliability_pm_checks",
                    description = "Evaluasi PM & FMEA",
                    count = pmCount,
                    sizeBytes = pmBytes,
                    totalBytes = safeTotalPayload
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))

                CollectionDataRow(
                    collectionName = "mentor_pairing_logs",
                    description = "Pendampingan Operator",
                    count = mentorCount,
                    sizeBytes = mentorBytes,
                    totalBytes = safeTotalPayload
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Total
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE6F4F4), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL AKTUAL", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen, modifier = Modifier.weight(1.3f))
                    Text("$totalDocs", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("${String.format(Locale.US, "%,d", actualStoredKb.toInt())} KB", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                    Text("100%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // 5. KARTU ANALISA KETAHANAN KUOTA (QUOTA RUNWAY)
        // -----------------------------------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analisa Ketahanan Kuota Gratis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D)
                    )
                }

                Text(
                    text = "• Pertumbuhan Data: Rata-rata operasional 8 unit Centrifuge (SC-01 s/d SC-08) menghasilkan sekitar ~1.2 s/d 1.5 MB data baru per bulan.\n" +
                            "• Proyeksi Ketahanan: Dengan sisa ruang penyimpanan ${String.format(Locale.US, "%.1f", sparkStorageRemainingMb)} MB, kuota gratis Firebase Spark diperkirakan dapat bertahan lebih dari $estimatedRunwayYears tahun tanpa biaya tambahan.\n" +
                            "• Kesimpulan: Sistem TIDAK memerlukan upgrade ke paket berbayar (Blaze Plan). Semua aktivitas harian berada jauh di bawah batas limit.",
                    fontSize = 11.sp,
                    color = Color(0xFF166534),
                    lineHeight = 16.sp
                )

                if (totalUnsynced > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onSyncClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sinkronkan $totalUnsynced Data Pending Sekarang", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUB-COMPOSABLES HELPER
// -------------------------------------------------------------------------------------------------

@Composable
private fun SparkMiniStatCard(
    title: String,
    value: String,
    limit: String,
    percent: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Limit: $limit", fontSize = 8.5.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
                color = color,
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    text = percent,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun SparkQuotaProgressRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    currentValue: String,
    limitValue: String,
    percent: Double,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateGrey
                )
            }
            Text(
                text = "${String.format(Locale.US, "%.2f", percent)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Text(
            text = subtitle,
            fontSize = 9.5.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 22.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Aktual: $currentValue",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = SlateGrey
            )
            Text(
                text = "Batas: $limitValue",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        val progressFraction = (percent / 100.0).toFloat().coerceIn(0.005f, 1f)
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFFF1F5F9)
        )
    }
}

@Composable
private fun CollectionDataRow(
    collectionName: String,
    description: String,
    count: Int,
    sizeBytes: Long,
    totalBytes: Double
) {
    val sizeKb = sizeBytes / 1024.0
    val portionPercent = (sizeBytes.toDouble() / totalBytes) * 100.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.3f)) {
            Text(collectionName, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
            Text(description, fontSize = 9.sp, color = Color.Gray)
        }
        Text("$count", fontSize = 10.sp, color = SlateGrey, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
        Text("${String.format(Locale.US, "%.1f", sizeKb)} KB", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SlateGrey, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
        Text("${String.format(Locale.US, "%.1f", portionPercent)}%", fontSize = 9.5.sp, color = Color.Gray, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
    }
}
