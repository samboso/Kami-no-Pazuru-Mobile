package com.example.sudoku

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.sudoku.data.UserPrefs
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StatsActivity : BaseDrawerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        initDrawer(R.id.toolbar_stats)

        val tvUser  = findViewById<TextView>(R.id.tv_user)
        val tvSolved= findViewById<TextView>(R.id.tv_solved)
        val tvTime  = findViewById<TextView>(R.id.tv_total)
        val img     = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.img_stats_avatar)

        lifecycleScope.launch {
            combine(
                UserPrefs.profile(this@StatsActivity),
                UserPrefs.stats(this@StatsActivity)
            ){ p,s -> p to s }.collect{ (p,s) ->
                tvUser.text = p.name
                img.load(p.avatar)
                tvSolved.text = "Sudokus resueltos: ${s.solved}"
                val mins = s.totalTime / 60000
                tvTime.text = "Tiempo total: $mins min"
            }
        }
        val chart = findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.line_chart)
        lifecycleScope.launch {
            UserPrefs.dailyAverages(this@StatsActivity).collect { map ->
                val entries = map.entries.sortedBy { it.key }.mapIndexed { i, e ->
                    com.github.mikephil.charting.data.Entry(i.toFloat(), e.value / 60000f)
                }
                val set = com.github.mikephil.charting.data.LineDataSet(entries, "Minutos/día").apply {
                    setDrawCircles(false); lineWidth = 2f
                }
                chart.data = com.github.mikephil.charting.data.LineData(set)
                chart.axisRight.isEnabled = false
                chart.description.isEnabled = false
                chart.invalidate()
            }
        }
    }
}
