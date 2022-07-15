package com.example.businessmanagement

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.mahozad.android.PieChart
import kotlinx.android.synthetic.main.activity_franchiser_dashboard.*

class FranchiserDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchiser_dashboard)

        supportActionBar?.hide()

        pieChart.slices = listOf(
            PieChart.Slice(0.2f, Color.BLUE),
            PieChart.Slice(0.4f, Color.MAGENTA),
            PieChart.Slice(0.3f, Color.YELLOW),
            PieChart.Slice(0.1f, Color.CYAN)
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}