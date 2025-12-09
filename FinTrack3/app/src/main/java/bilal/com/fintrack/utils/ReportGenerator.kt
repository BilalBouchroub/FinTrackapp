package bilal.com.fintrack.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.core.content.FileProvider
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.ui.screens.statistics.CategoryStat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Générateur de rapports PDF et CSV avec graphiques et statistiques complètes
 */
object ReportGenerator {

    /**
     * Exporter les statistiques en PDF avec graphiques
     * @param context Context de l'application
     * @param transactions Liste de toutes les transactions
     * @param categories Liste de toutes les catégories
     * @param categoryStats Statistiques par catégorie
     * @param totalBalance Solde total
     * @param totalIncome Total des revenus
     * @param totalExpense Total des dépenses
     * @param chartView Vue du graphique à capturer (optionnel)
     */
    fun generateStatisticsPdf(
        context: Context,
        transactions: List<Transaction>,
        categories: List<Category>,
        categoryStats: List<CategoryStat>,
        totalBalance: Double,
        totalIncome: Double,
        totalExpense: Double,
        chartView: View? = null
    ): File? {
        val fileName = "FinTrack_Statistiques_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        try {
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            // En-tête du rapport
            document.add(
                Paragraph("FinTrack - Rapport Statistiques")
                    .setFontSize(24f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(DeviceRgb(108, 99, 255))
            )
            
            document.add(
                Paragraph("Généré le: ${SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.FRENCH).format(Date())}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
            )
            
            document.add(Paragraph("\n"))
            
            // Résumé financier
            document.add(
                Paragraph("Résumé Financier")
                    .setFontSize(18f)
                    .setBold()
                    .setFontColor(DeviceRgb(108, 99, 255))
            )
            
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
                .useAllAvailableWidth()
            
            summaryTable.addCell(createHeaderCell("Total Revenus"))
            summaryTable.addCell(createValueCell("${totalIncome.toInt()} MAD", DeviceRgb(76, 175, 80)))
            
            summaryTable.addCell(createHeaderCell("Total Dépenses"))
            summaryTable.addCell(createValueCell("${totalExpense.toInt()} MAD", DeviceRgb(244, 67, 54)))
            
            summaryTable.addCell(createHeaderCell("Solde Total"))
            val balanceColor = if (totalBalance >= 0) DeviceRgb(76, 175, 80) else DeviceRgb(244, 67, 54)
            summaryTable.addCell(createValueCell("${totalBalance.toInt()} MAD", balanceColor))
            
            document.add(summaryTable)
            document.add(Paragraph("\n"))
            
            // Graphique (si fourni)
            if (chartView != null) {
                try {
                    val chartBitmap = captureView(chartView)
                    val chartImage = Image(ImageDataFactory.create(bitmapToByteArray(chartBitmap)))
                        .setWidth(UnitValue.createPercentValue(80f))
                        .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                    document.add(chartImage)
                    document.add(Paragraph("\n"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Statistiques par catégorie
            document.add(
                Paragraph("Détails par Catégorie")
                    .setFontSize(18f)
                    .setBold()
                    .setFontColor(DeviceRgb(108, 99, 255))
            )
            
            val categoryTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 2f, 1f)))
                .useAllAvailableWidth()
            
            categoryTable.addHeaderCell(createHeaderCell("Catégorie"))
            categoryTable.addHeaderCell(createHeaderCell("Type"))
            categoryTable.addHeaderCell(createHeaderCell("Montant"))
            categoryTable.addHeaderCell(createHeaderCell("Pourcentage"))
            
            categoryStats.forEach { stat ->
                categoryTable.addCell(createDataCell(stat.categoryName))
                categoryTable.addCell(createDataCell(
                    when (stat.type) {
                        bilal.com.fintrack.data.local.entities.TransactionType.INCOME -> "Revenu"
                        bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE -> "Dépense"
                        bilal.com.fintrack.data.local.entities.TransactionType.DEBT -> "Dette/Prêt"
                    }
                ))
                categoryTable.addCell(createDataCell("${stat.amount.toInt()} MAD"))
                categoryTable.addCell(createDataCell("${stat.percentage.toInt()}%"))
            }
            
            document.add(categoryTable)
            document.add(Paragraph("\n"))
            
            // Liste des transactions récentes (dernières 20)
            document.add(
                Paragraph("Transactions Récentes")
                    .setFontSize(18f)
                    .setBold()
                    .setFontColor(DeviceRgb(108, 99, 255))
            )
            
            val transactionTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 2f, 2f, 2f, 3f)))
                .useAllAvailableWidth()
            
            transactionTable.addHeaderCell(createHeaderCell("Date"))
            transactionTable.addHeaderCell(createHeaderCell("Type"))
            transactionTable.addHeaderCell(createHeaderCell("Catégorie"))
            transactionTable.addHeaderCell(createHeaderCell("Montant"))
            transactionTable.addHeaderCell(createHeaderCell("Notes"))
            
            transactions.sortedByDescending { it.date }.take(20).forEach { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                transactionTable.addCell(createDataCell(DateUtils.formatDate(transaction.date)))
                transactionTable.addCell(createDataCell(
                    when (transaction.type) {
                        bilal.com.fintrack.data.local.entities.TransactionType.INCOME -> "Revenu"
                        bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE -> "Dépense"
                        bilal.com.fintrack.data.local.entities.TransactionType.DEBT -> "Dette/Prêt"
                    }
                ))
                transactionTable.addCell(createDataCell(category?.name ?: "Inconnue"))
                transactionTable.addCell(createDataCell("${transaction.amount.toInt()} MAD"))
                transactionTable.addCell(createDataCell(transaction.notes ?: "-"))
            }
            
            document.add(transactionTable)
            
            // Pied de page
            document.add(Paragraph("\n\n"))
            document.add(
                Paragraph("Généré par FinTrack - Votre gestionnaire de budget personnel")
                    .setFontSize(8f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
            )
            
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Exporter les statistiques en CSV avec tableau détaillé
     * Format: Solde, Transaction, Catégorie, Montant, Budget
     */
    fun generateStatisticsCsv(
        context: Context,
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<Budget>,
        totalBalance: Double
    ): File? {
        val fileName = "FinTrack_Export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(Date())}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        try {
            FileOutputStream(file).use { fos ->
                // En-tête du fichier
                val header = "Date,Type,Catégorie,Montant (MAD),Budget Catégorie (MAD),Méthode Paiement,Notes,Solde Total (MAD)\n"
                fos.write(header.toByteArray())
                
                // Trier les transactions par date (plus récentes en premier)
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    val category = categories.find { it.id == transaction.categoryId }
                    val budget = budgets.find { it.categoryId == transaction.categoryId }
                    
                    val type = when (transaction.type) {
                        bilal.com.fintrack.data.local.entities.TransactionType.INCOME -> "Revenu"
                        bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE -> "Dépense"
                        bilal.com.fintrack.data.local.entities.TransactionType.DEBT -> "Dette/Prêt"
                    }
                    
                    val line = "${DateUtils.formatDate(transaction.date)}," +
                            "$type," +
                            "${category?.name ?: "Inconnue"}," +
                            "${transaction.amount}," +
                            "${budget?.amount ?: "N/A"}," +
                            "${transaction.paymentMethod}," +
                            "\"${transaction.notes?.replace("\"", "\"\"") ?: ""}\"," +
                            "$totalBalance\n"
                    
                    fos.write(line.toByteArray())
                }
                
                // Ligne de résumé
                fos.write("\n".toByteArray())
                fos.write("RÉSUMÉ\n".toByteArray())
                fos.write("Solde Total,$totalBalance MAD\n".toByteArray())
                
                val totalIncome = transactions.filter { 
                    it.type == bilal.com.fintrack.data.local.entities.TransactionType.INCOME 
                }.sumOf { it.amount }
                fos.write("Total Revenus,$totalIncome MAD\n".toByteArray())
                
                val totalExpense = transactions.filter { 
                    it.type == bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE 
                }.sumOf { it.amount }
                fos.write("Total Dépenses,$totalExpense MAD\n".toByteArray())
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Partager un fichier généré
     */
    fun shareFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val mimeType = when {
            file.name.endsWith(".pdf") -> "application/pdf"
            file.name.endsWith(".csv") -> "text/csv"
            else -> "*/*"
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Partager le rapport"))
    }
    
    // Fonctions utilitaires pour le PDF
    
    private fun createHeaderCell(text: String): Cell {
        return Cell().add(Paragraph(text))
            .setBackgroundColor(DeviceRgb(108, 99, 255))
            .setFontColor(ColorConstants.WHITE)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8f)
    }
    
    private fun createValueCell(text: String, color: DeviceRgb): Cell {
        return Cell().add(Paragraph(text))
            .setFontColor(color)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8f)
    }
    
    private fun createDataCell(text: String): Cell {
        return Cell().add(Paragraph(text))
            .setTextAlignment(TextAlignment.LEFT)
            .setPadding(5f)
    }
    
    private fun captureView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
