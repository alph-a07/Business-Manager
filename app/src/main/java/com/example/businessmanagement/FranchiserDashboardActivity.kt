package com.example.businessmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.businessmanagement.model.User
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_franchiser_dashboard.*


class FranchiserDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchiser_dashboard)

        supportActionBar?.hide()

        iv_franchisee_dashboard_account.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        floatingActionButton.setOnClickListener {
            startActivity(Intent(this, BusinessRegisterFormActivity::class.java))
        }

        showPieChart(pieChart)
    }

    override fun onStart() {
        super.onStart()

        val ref = Firebase.database
        val currUser = FirebaseAuth.getInstance().currentUser

        ref.getReference("Users").orderByChild("uid").equalTo(currUser!!.uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                val model = snap.getValue(User::class.java)
                                var approvedBusinesses = 0

                                for (tmp in model!!.list!!) {
                                    if (tmp.isApproved)
                                        approvedBusinesses++
                                }
                                tv_franchiser_dashboard_num_of_registered_businesses.text =
                                    approvedBusinesses.toString()

                                textView2.text = "Hi, " + model.userName + " 👋"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                }
            )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    private fun showPieChart(pieChart: PieChart) {
        // pie chart customisations
        pieChart.isDrawHoleEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12F)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)

        // legend customisations
        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = true

        // pie chart data
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(100f, "Food & Dining"))
        entries.add(PieEntry(200f, "Medical"))
        entries.add(PieEntry(500f, "Entertainment"))
        entries.add(PieEntry(800f, "Properties"))
        entries.add(PieEntry(1000f, "Education"))

        // pie chart colors
        val colors: ArrayList<Int> = ArrayList()
        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }

        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setDrawValues(false)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextColor(Color.BLACK)

        pieChart.data = data
        pieChart.invalidate()

        pieChart.animateY(1400, Easing.EaseInOutQuad)
    }
}
