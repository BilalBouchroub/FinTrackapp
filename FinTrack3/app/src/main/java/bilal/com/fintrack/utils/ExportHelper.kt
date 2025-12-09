package bilal.com.fintrack.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import bilal.com.fintrack.data.local.entities.Transaction
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun exportToCsv(context: Context, transactions: List<Transaction>): File? {
        val fileName = "fintrack_export_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        try {
            FileOutputStream(file).use { fos ->
                val header = "Date,Type,Category,Amount,PaymentMethod,Notes\n"
                fos.write(header.toByteArray())
                
                transactions.forEach { t ->
                    val line = "${DateUtils.formatDate(t.date)},${t.type},${t.categoryId},${t.amount},${t.paymentMethod},${t.notes}\n"
                    fos.write(line.toByteArray())
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportToPdf(context: Context, transactions: List<Transaction>): File? {
        val fileName = "fintrack_report_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        try {
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            document.add(Paragraph("FinTrack Monthly Report").setFontSize(24f).setBold())
            document.add(Paragraph("Generated on: ${DateUtils.formatDate(System.currentTimeMillis())}"))
            
            val table = Table(floatArrayOf(3f, 2f, 2f, 2f, 4f))
            table.addCell("Date")
            table.addCell("Type")
            table.addCell("Amount")
            table.addCell("Payment")
            table.addCell("Notes")
            
            transactions.forEach { t ->
                table.addCell(DateUtils.formatDate(t.date))
                table.addCell(t.type.name)
                table.addCell("${t.amount} MAD")
                table.addCell(t.paymentMethod)
                table.addCell(t.notes ?: "")
            }
            
            document.add(table)
            document.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun shareFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.name.endsWith(".pdf")) "application/pdf" else "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
