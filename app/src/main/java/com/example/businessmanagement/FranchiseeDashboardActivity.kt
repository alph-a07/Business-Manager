package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_franchisee_dashboard.*

class FranchiseeDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_dashboard)

        supportActionBar?.hide()

        iv_franchisee_dashboard_account.setOnClickListener {
            startActivity(Intent(this,AccountActivity::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onStart() {
        super.onStart()

    }
}