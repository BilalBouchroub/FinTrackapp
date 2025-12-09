package bilal.com.fintrack.ui.components

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * Composable pour afficher un graphique en ligne des dépenses
 * Utilise MPAndroidChart LineChart
 */
@Composable
fun ExpenseLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    labels: List<String> = dataPoints.indices.map { "J$it" }
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Configuration du graphique
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                
                // Configuration de l'axe X
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(labels)
                    textColor = Color.GRAY
                }
                
                // Configuration de l'axe Y gauche
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.GRAY
                }
                
                // Désactiver l'axe Y droit
                axisRight.isEnabled = false
                
                // Configuration de la légende
                legend.isEnabled = false
            }
        },
        update = { lineChart ->
            val entries = dataPoints.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }
            
            val dataSet = LineDataSet(entries, "Dépenses").apply {
                color = Color.parseColor("#6C63FF")
                setCircleColor(Color.parseColor("#6C63FF"))
                circleRadius = 4f
                lineWidth = 2.5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = Color.parseColor("#6C63FF")
                fillAlpha = 50
            }
            
            lineChart.data = LineData(dataSet)
            lineChart.animateX(1000)
            lineChart.invalidate()
        }
    )
}

/**
 * Composable pour afficher un graphique en barres des revenus et dépenses
 * Utilise MPAndroidChart BarChart
 */
@Composable
fun IncomeExpenseBarChart(
    income: List<Float>,
    expense: List<Float>,
    modifier: Modifier = Modifier,
    labels: List<String> = income.indices.map { "M$it" }
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Configuration du graphique
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                
                // Configuration de l'axe X
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(labels)
                    textColor = Color.GRAY
                }
                
                // Configuration de l'axe Y gauche
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.GRAY
                    axisMinimum = 0f
                }
                
                // Désactiver l'axe Y droit
                axisRight.isEnabled = false
                
                // Configuration de la légende
                legend.apply {
                    isEnabled = true
                    textColor = Color.GRAY
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                }
            }
        },
        update = { barChart ->
            val incomeEntries = income.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }
            
            val expenseEntries = expense.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }
            
            val incomeDataSet = BarDataSet(incomeEntries, "Revenus").apply {
                color = Color.parseColor("#4CAF50")
                valueTextColor = Color.DKGRAY
                setDrawValues(false)
            }
            
            val expenseDataSet = BarDataSet(expenseEntries, "Dépenses").apply {
                color = Color.parseColor("#F44336")
                valueTextColor = Color.DKGRAY
                setDrawValues(false)
            }
            
            val barData = BarData(incomeDataSet, expenseDataSet).apply {
                barWidth = 0.35f
            }
            
            barChart.data = barData
            
            // Grouper les barres
            val groupSpace = 0.1f
            val barSpace = 0.05f
            barChart.groupBars(0f, groupSpace, barSpace)
            
            barChart.animateY(1000)
            barChart.invalidate()
        }
    )
}
