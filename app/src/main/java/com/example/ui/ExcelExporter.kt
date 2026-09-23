package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.*
import com.example.util.WibDateUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    private fun escapeCsv(value: String): String {
        var str = value.replace("\r", " ").replace("\n", " ")
        if (str.contains(",") || str.contains("\"") || str.contains(";") || str.contains("\t")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }

    private fun shareExcelCsv(context: Context, filename: String, content: String) {
        try {
            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(cacheDir, filename)
            
            // Write UTF-8 BOM so Microsoft Excel immediately recognizes UTF-8 encoding properly
            FileOutputStream(file).use { fos ->
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, filename.removeSuffix(".csv"))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Buka atau Bagikan Data Excel ($filename)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportMonitoringExcel(
        context: Context,
        vibrationLogs: List<VibrationLog>,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        val dateFormat = WibDateUtils.createFormatter("yyyy-MM-dd HH:mm:ss 'WIB'")
        val dateOnlyFormat = WibDateUtils.createFormatter("dd MMM yyyy")
        val fileDate = if (startDate != null && endDate != null) {
            "${WibDateUtils.format("yyyyMMdd", Date(startDate))}_sd_${WibDateUtils.format("yyyyMMdd", Date(endDate))}"
        } else {
            WibDateUtils.format("yyyyMMdd_HHmm", Date())
        }

        val filteredLogs = if (startDate != null && endDate != null) {
            vibrationLogs.filter { it.timestamp in startDate..endDate }
        } else {
            vibrationLogs
        }

        if (filteredLogs.isEmpty()) {
            Toast.makeText(context, "Tidak ada data monitoring pada rentang tanggal yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()

        sb.append("Laporan Monitoring & Trend Analysis Centrifuge (SC-01 s/d SC-08)\n")
        sb.append("Tanggal Export:,").append(escapeCsv(dateFormat.format(Date()))).append("\n")
        if (startDate != null && endDate != null) {
            sb.append("Periode Data:,").append(escapeCsv("${dateOnlyFormat.format(Date(startDate))} s/d ${dateOnlyFormat.format(Date(endDate))}")).append("\n")
        } else {
            sb.append("Periode Data:,Semua Data\n")
        }
        sb.append("Total Data:,").append(filteredLogs.size).append("\n\n")

        sb.append("ID,Timestamp,Waktu,Machine/Unit,DE Vibration (mm/s),NDE Vibration (mm/s),Motor Vibration (mm/s),Gearbox Vibration (mm/s),Bowl Vibration (mm/s),Bearing Temp (°C),Motor Temp (°C),Alarm State,Greasing,Leakage,Sound State,Catatan / Parameter\n")

        filteredLogs.sortedByDescending { it.timestamp }.forEach { log ->
            val dateStr = dateFormat.format(Date(log.timestamp))
            val machineName = log.comments.lines().firstOrNull()?.take(30) ?: "SC Centrifuge"
            val greasedStr = when {
                log.greasingStatus == "Belum Masuk Jadwal" -> "Belum Masuk Jadwal"
                log.greasingStatus == "Ya" || log.isGreased -> "Sudah"
                else -> "Belum"
            }
            val leakStr = if (log.hasLeakage) "Ada Kebocoran" else "Normal"

            sb.append(log.id).append(",")
                .append(log.timestamp).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(machineName)).append(",")
                .append(String.format(Locale.US, "%.2f", log.driveEndVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.nonDriveEndVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.motorBearingVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.gearboxBearingVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.bowlVibration)).append(",")
                .append(String.format(Locale.US, "%.1f", log.bearingTemp)).append(",")
                .append(String.format(Locale.US, "%.1f", log.motorTemp)).append(",")
                .append(escapeCsv(log.alarmState)).append(",")
                .append(escapeCsv(greasedStr)).append(",")
                .append(escapeCsv(leakStr)).append(",")
                .append(escapeCsv(log.soundState)).append(",")
                .append(escapeCsv(log.comments)).append("\n")
        }

        shareExcelCsv(context, "Laporan_Monitoring_Trend_Centrifuge_$fileDate.csv", sb.toString())
    }

    fun exportCiltExcel(
        context: Context,
        ciltChecks: List<CiltCheck>,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        val dateFormat = WibDateUtils.createFormatter("yyyy-MM-dd HH:mm:ss 'WIB'")
        val dateOnlyFormat = WibDateUtils.createFormatter("dd MMM yyyy")
        val fileDate = if (startDate != null && endDate != null) {
            "${WibDateUtils.format("yyyyMMdd", Date(startDate))}_sd_${WibDateUtils.format("yyyyMMdd", Date(endDate))}"
        } else {
            WibDateUtils.format("yyyyMMdd_HHmm", Date())
        }

        val filteredChecks = if (startDate != null && endDate != null) {
            ciltChecks.filter { it.timestamp in startDate..endDate }
        } else {
            ciltChecks
        }

        if (filteredChecks.isEmpty()) {
            Toast.makeText(context, "Tidak ada data CILT pada rentang tanggal yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()

        sb.append("Laporan Pelaksanaan CILT Centrifuge\n")
        sb.append("Tanggal Export:,").append(escapeCsv(dateFormat.format(Date()))).append("\n")
        if (startDate != null && endDate != null) {
            sb.append("Periode Data:,").append(escapeCsv("${dateOnlyFormat.format(Date(startDate))} s/d ${dateOnlyFormat.format(Date(endDate))}")).append("\n")
        } else {
            sb.append("Periode Data:,Semua Data\n")
        }
        sb.append("Total Data:,").append(filteredChecks.size).append("\n\n")

        sb.append("ID,Timestamp,Waktu,Operator,Area Bersih (Sludge/Oli),Mesin Bebas Kerak,Drainase Tidak Sumbat,Catatan Cleaning,Vibrasi & Suara Normal,Temperatur Normal,Tidak Ada Kebocoran,Kondisi Komponen Baik,Catatan Inspection,Greasing Sesuai Jadwal,Level Oli Normal,Catatan Lubrication,Baut Pondasi Dikencangkan,Tidak Ada Baut Longgar/Lepas,Catatan Tightening,Catatan Umum\n")

        filteredChecks.sortedByDescending { it.timestamp }.forEach { c ->
            val dateStr = dateFormat.format(Date(c.timestamp))
            fun bStr(b: Boolean) = if (b) "OK" else "Tidak OK / Belum"

            sb.append(c.id).append(",")
                .append(c.timestamp).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(c.operatorName)).append(",")
                .append(bStr(c.isAreaCleaned)).append(",")
                .append(bStr(c.isMachineCleaned)).append(",")
                .append(bStr(c.isDrainageCleaned)).append(",")
                .append(escapeCsv(c.cleaningNotes)).append(",")
                .append(bStr(c.isVibrationSoundChecked)).append(",")
                .append(bStr(c.isTemperatureChecked)).append(",")
                .append(bStr(c.isLeakChecked)).append(",")
                .append(bStr(c.isComponentsConditionChecked)).append(",")
                .append(escapeCsv(c.inspectionNotes)).append(",")
                .append(bStr(c.isGreasingBearingChecked)).append(",")
                .append(bStr(c.isOilLevelChecked)).append(",")
                .append(escapeCsv(c.lubricationNotes)).append(",")
                .append(bStr(c.isFoundationBoltsTightened)).append(",")
                .append(bStr(c.isNoLooseBoltsChecked)).append(",")
                .append(escapeCsv(c.tighteningNotes)).append(",")
                .append(escapeCsv(c.comments)).append("\n")
        }

        shareExcelCsv(context, "Laporan_CILT_Centrifuge_$fileDate.csv", sb.toString())
    }

    fun exportReliabilityPmExcel(
        context: Context,
        reports: List<AbnormalityReport>,
        reliabilityChecks: List<ReliabilityPmCheck>,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        val dateFormat = WibDateUtils.createFormatter("yyyy-MM-dd HH:mm:ss 'WIB'")
        val dateOnlyFormat = WibDateUtils.createFormatter("dd MMM yyyy")
        val fileDate = if (startDate != null && endDate != null) {
            "${WibDateUtils.format("yyyyMMdd", Date(startDate))}_sd_${WibDateUtils.format("yyyyMMdd", Date(endDate))}"
        } else {
            WibDateUtils.format("yyyyMMdd_HHmm", Date())
        }

        val filteredReports = if (startDate != null && endDate != null) {
            reports.filter { it.timestamp in startDate..endDate }
        } else {
            reports
        }
        val filteredChecks = if (startDate != null && endDate != null) {
            reliabilityChecks.filter { it.timestamp in startDate..endDate }
        } else {
            reliabilityChecks
        }

        if (filteredReports.isEmpty() && filteredChecks.isEmpty()) {
            Toast.makeText(context, "Tidak ada data Reliability PM pada rentang tanggal yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()

        sb.append("Laporan Real-Time Reliability PM & Abnormality Findings\n")
        sb.append("Tanggal Export:,").append(escapeCsv(dateFormat.format(Date()))).append("\n")
        if (startDate != null && endDate != null) {
            sb.append("Periode Data:,").append(escapeCsv("${dateOnlyFormat.format(Date(startDate))} s/d ${dateOnlyFormat.format(Date(endDate))}")).append("\n")
        } else {
            sb.append("Periode Data:,Semua Data\n")
        }
        sb.append("Total Abnormality Reports:,").append(filteredReports.size).append("\n")
        sb.append("Total PM Checks:,").append(filteredChecks.size).append("\n\n")

        sb.append("=== DAFTAR TEMUAN ABNORMALITY & TAGGING ===\n")
        sb.append("ID,Waktu,Judul / Temuan,Tag Type,RPN Score,Severity,Occurrence,Detection,Faktor 4M,PIC Pelapor,Deskripsi Temuan\n")

        filteredReports.sortedByDescending { it.timestamp }.forEach { rep ->
            val dateStr = dateFormat.format(Date(rep.timestamp))
            sb.append(rep.id).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(rep.title)).append(",")
                .append(escapeCsv(rep.tagType)).append(",")
                .append(rep.rpn).append(",")
                .append(rep.severityScore).append(",")
                .append(rep.occurrenceScore).append(",")
                .append(rep.detectionScore).append(",")
                .append(escapeCsv(rep.factor)).append(",")
                .append(escapeCsv(rep.picName)).append(",")
                .append(escapeCsv(rep.description)).append("\n")
        }

        if (filteredChecks.isNotEmpty()) {
            sb.append("\n=== LOG RELIABILITY PM CHECKLIST ===\n")
            sb.append("ID,Waktu,Flushing Interval (Menit),Suhu Air (°C),Hollow Bearing,Coupling Leak,Vibration Check,Bowl Speed (RPM),Bearing Temp (°C),Unusual Noise,Catatan\n")
            filteredChecks.sortedByDescending { it.timestamp }.forEach { chk ->
                val dateStr = dateFormat.format(Date(chk.timestamp))
                fun bStr(b: Boolean) = if (b) "OK / Checked" else "Belum"
                sb.append(chk.id).append(",")
                    .append(escapeCsv(dateStr)).append(",")
                    .append(chk.flushingIntervalMinutes).append(",")
                    .append(String.format(Locale.US, "%.1f", chk.waterTempCelsius)).append(",")
                    .append(bStr(chk.hollowBearingChecked)).append(",")
                    .append(if (chk.couplingOilLeakChecked) "Ada Bocor" else "Normal").append(",")
                    .append(bStr(chk.vibrationChecked)).append(",")
                    .append(String.format(Locale.US, "%.0f", chk.bowlSpeedRpm)).append(",")
                    .append(String.format(Locale.US, "%.1f", chk.bearingTempCelsius)).append(",")
                    .append(if (chk.unusualNoiseDetected) "Ada Suara Abnormal" else "Normal").append(",")
                    .append(escapeCsv(chk.comments)).append("\n")
            }
        }

        shareExcelCsv(context, "Laporan_Reliability_PM_$fileDate.csv", sb.toString())
    }

    fun exportFlushingExcel(
        context: Context,
        flushingLogs: List<FlushingLog>,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        val dateFormat = WibDateUtils.createFormatter("yyyy-MM-dd HH:mm:ss 'WIB'")
        val dateOnlyFormat = WibDateUtils.createFormatter("dd MMM yyyy")
        val fileDate = if (startDate != null && endDate != null) {
            "${WibDateUtils.format("yyyyMMdd", Date(startDate))}_sd_${WibDateUtils.format("yyyyMMdd", Date(endDate))}"
        } else {
            WibDateUtils.format("yyyyMMdd_HHmm", Date())
        }

        val filteredLogs = if (startDate != null && endDate != null) {
            flushingLogs.filter { it.timestamp in startDate..endDate }
        } else {
            flushingLogs
        }

        if (filteredLogs.isEmpty()) {
            Toast.makeText(context, "Tidak ada data Flushing pada rentang tanggal yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()

        sb.append("Laporan Kegiatan Flushing Centrifuge & Progres H+1\n")
        sb.append("Tanggal Export:,").append(escapeCsv(dateFormat.format(Date()))).append("\n")
        if (startDate != null && endDate != null) {
            sb.append("Periode Data:,").append(escapeCsv("${dateOnlyFormat.format(Date(startDate))} s/d ${dateOnlyFormat.format(Date(endDate))}")).append("\n")
        } else {
            sb.append("Periode Data:,Semua Data\n")
        }
        sb.append("Total Data:,").append(filteredLogs.size).append("\n\n")

        sb.append("ID,Timestamp,Waktu,Unit Mesin,Shift,Operator / PIC,Poin Checklist Selesai,Total Poin,Persentase Kepatuhan,Catatan Khusus\n")

        filteredLogs.sortedByDescending { it.timestamp }.forEach { fl ->
            val dateStr = dateFormat.format(Date(fl.timestamp))
            val percent = if (fl.totalCount > 0) (fl.completedCount.toFloat() / fl.totalCount * 100).toInt() else 0
            sb.append(fl.id).append(",")
                .append(fl.timestamp).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(fl.unitName)).append(",")
                .append(escapeCsv(fl.shift)).append(",")
                .append(escapeCsv(fl.operatorName)).append(",")
                .append(fl.completedCount).append(",")
                .append(fl.totalCount).append(",")
                .append("$percent%").append(",")
                .append(escapeCsv(fl.notes)).append("\n")
        }

        shareExcelCsv(context, "Laporan_Flushing_Centrifuge_$fileDate.csv", sb.toString())
    }

    fun exportAllReportsExcel(
        context: Context,
        vibrationLogs: List<VibrationLog>,
        ciltChecks: List<CiltCheck>,
        reports: List<AbnormalityReport>,
        reliabilityChecks: List<ReliabilityPmCheck>,
        flushingLogs: List<FlushingLog>,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        val dateFormat = WibDateUtils.createFormatter("yyyy-MM-dd HH:mm:ss 'WIB'")
        val dateOnlyFormat = WibDateUtils.createFormatter("dd MMM yyyy")
        val fileDate = if (startDate != null && endDate != null) {
            "${WibDateUtils.format("yyyyMMdd", Date(startDate))}_sd_${WibDateUtils.format("yyyyMMdd", Date(endDate))}"
        } else {
            WibDateUtils.format("yyyyMMdd_HHmm", Date())
        }

        val filteredVib = if (startDate != null && endDate != null) vibrationLogs.filter { it.timestamp in startDate..endDate } else vibrationLogs
        val filteredCilt = if (startDate != null && endDate != null) ciltChecks.filter { it.timestamp in startDate..endDate } else ciltChecks
        val filteredReports = if (startDate != null && endDate != null) reports.filter { it.timestamp in startDate..endDate } else reports
        val filteredReliability = if (startDate != null && endDate != null) reliabilityChecks.filter { it.timestamp in startDate..endDate } else reliabilityChecks
        val filteredFlushing = if (startDate != null && endDate != null) flushingLogs.filter { it.timestamp in startDate..endDate } else flushingLogs

        val totalRecords = filteredVib.size + filteredCilt.size + filteredReports.size + filteredReliability.size + filteredFlushing.size
        if (totalRecords == 0) {
            Toast.makeText(context, "Tidak ada data apapun pada rentang tanggal yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("LAPORAN OPERASIONAL LENGKAP CENTRIFUGE MILL 6321\n")
        sb.append("Tanggal Export:,").append(escapeCsv(dateFormat.format(Date()))).append("\n")
        if (startDate != null && endDate != null) {
            sb.append("Periode Data:,").append(escapeCsv("${dateOnlyFormat.format(Date(startDate))} s/d ${dateOnlyFormat.format(Date(endDate))}")).append("\n")
        } else {
            sb.append("Periode Data:,Semua Data\n")
        }
        sb.append("Ringkasan Total:,").append("Monitoring: ${filteredVib.size} | CILT: ${filteredCilt.size} | Abnormality: ${filteredReports.size} | PM: ${filteredReliability.size} | Flushing: ${filteredFlushing.size}\n\n")

        // 1. Monitoring SC
        sb.append("=== 1. MONITORING & TREND ANALYSIS CENTRIFUGE ===\n")
        sb.append("ID,Timestamp,Waktu,Machine/Unit,DE Vibration,NDE Vibration,Motor Vibration,Gearbox Vibration,Bowl Vibration,Bearing Temp,Motor Temp,Alarm State,Greasing,Leakage,Sound State,Catatan\n")
        filteredVib.sortedByDescending { it.timestamp }.forEach { log ->
            val dateStr = dateFormat.format(Date(log.timestamp))
            val machineName = log.comments.lines().firstOrNull()?.take(30) ?: "SC Centrifuge"
            val greasedStr = when {
                log.greasingStatus == "Belum Masuk Jadwal" -> "Belum Masuk Jadwal"
                log.greasingStatus == "Ya" || log.isGreased -> "Sudah"
                else -> "Belum"
            }
            val leakStr = if (log.hasLeakage) "Ada Kebocoran" else "Normal"
            sb.append(log.id).append(",")
                .append(log.timestamp).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(machineName)).append(",")
                .append(String.format(Locale.US, "%.2f", log.driveEndVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.nonDriveEndVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.motorBearingVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.gearboxBearingVibration)).append(",")
                .append(String.format(Locale.US, "%.2f", log.bowlVibration)).append(",")
                .append(String.format(Locale.US, "%.1f", log.bearingTemp)).append(",")
                .append(String.format(Locale.US, "%.1f", log.motorTemp)).append(",")
                .append(escapeCsv(log.alarmState)).append(",")
                .append(escapeCsv(greasedStr)).append(",")
                .append(escapeCsv(leakStr)).append(",")
                .append(escapeCsv(log.soundState)).append(",")
                .append(escapeCsv(log.comments)).append("\n")
        }

        // 2. CILT
        sb.append("\n=== 2. LAPORAN PELAKSANAAN CILT CENTRIFUGE ===\n")
        sb.append("ID,Timestamp,Waktu,Operator,Nozzle Cleaned,Bowl Cleaned,Area Cleaned,Nozzle Checked,Vibration Checked,Leak Checked,Instrument Checked,Bearing Greased,Coupling Greased,Oil Level Checked,Nozzle Bolts Tightened,Fitting Pipes Tightened,Belt Tension Checked,Catatan\n")
        filteredCilt.sortedByDescending { it.timestamp }.forEach { c ->
            val dateStr = dateFormat.format(Date(c.timestamp))
            fun bStr(b: Boolean) = if (b) "OK" else "Belum"
            sb.append(c.id).append(",")
                .append(c.timestamp).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(c.operatorName)).append(",")
                .append(bStr(c.nozzleCleaned)).append(",")
                .append(bStr(c.bowlCleaned)).append(",")
                .append(bStr(c.areaCleaned)).append(",")
                .append(bStr(c.nozzleChecked)).append(",")
                .append(bStr(c.vibrationChecked)).append(",")
                .append(bStr(c.leakChecked)).append(",")
                .append(bStr(c.instrumentChecked)).append(",")
                .append(bStr(c.bearingGreased)).append(",")
                .append(bStr(c.couplingGreased)).append(",")
                .append(bStr(c.oilLevelChecked)).append(",")
                .append(bStr(c.nozzleBoltsTightened)).append(",")
                .append(bStr(c.fittingPipesTightened)).append(",")
                .append(bStr(c.beltTensionChecked)).append(",")
                .append(escapeCsv(c.comments)).append("\n")
        }

        // 3. Reliability & Abnormality
        sb.append("\n=== 3. TEMUAN ABNORMALITY & RELIABILITY PM ===\n")
        sb.append("ID,Waktu,Judul / Temuan,Tag Type,RPN Score,Severity,Occurrence,Detection,Faktor 4M,PIC Pelapor,Deskripsi\n")
        filteredReports.sortedByDescending { it.timestamp }.forEach { rep ->
            val dateStr = dateFormat.format(Date(rep.timestamp))
            sb.append(rep.id).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(rep.title)).append(",")
                .append(escapeCsv(rep.tagType)).append(",")
                .append(rep.rpn).append(",")
                .append(rep.severityScore).append(",")
                .append(rep.occurrenceScore).append(",")
                .append(rep.detectionScore).append(",")
                .append(escapeCsv(rep.factor)).append(",")
                .append(escapeCsv(rep.picName)).append(",")
                .append(escapeCsv(rep.description)).append("\n")
        }

        // 4. Flushing
        sb.append("\n=== 4. KEGIATAN FLUSHING CENTRIFUGE ===\n")
        sb.append("ID,Timestamp,Waktu,Unit Mesin,Shift,Operator,Poin Selesai,Total Poin,Kepatuhan,Catatan\n")
        filteredFlushing.sortedByDescending { it.timestamp }.forEach { fl ->
            val dateStr = dateFormat.format(Date(fl.timestamp))
            val percent = if (fl.totalCount > 0) (fl.completedCount.toFloat() / fl.totalCount * 100).toInt() else 0
            sb.append(fl.id).append(",")
                .append(fl.timestamp).append(",")
                .append(escapeCsv(dateStr)).append(",")
                .append(escapeCsv(fl.unitName)).append(",")
                .append(escapeCsv(fl.shift)).append(",")
                .append(escapeCsv(fl.operatorName)).append(",")
                .append(fl.completedCount).append(",")
                .append(fl.totalCount).append(",")
                .append("$percent%").append(",")
                .append(escapeCsv(fl.notes)).append("\n")
        }

        shareExcelCsv(context, "Laporan_Lengkap_Operasional_Centrifuge_$fileDate.csv", sb.toString())
    }
}
