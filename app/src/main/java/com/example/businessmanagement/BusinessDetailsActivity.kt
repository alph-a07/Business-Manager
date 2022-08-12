package com.example.businessmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_business_details.*

class BusinessDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_details)

        supportActionBar?.hide()
        btm.bringToFront()

        iv_back_arrow.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        tv_business_name.text = intent.getStringExtra("name")
        tv_business_category.text = intent.getStringExtra("category")
        tv_business_revenue.text = intent.getStringExtra("revenue")
        tv_business_info.text = intent.getStringExtra("description")
        tv_website.text = intent.getStringExtra("website")
        tv_location.text = intent.getStringExtra("address")
        tv_email.text = intent.getStringExtra("mail")
        tv_phone.text = intent.getStringExtra("phone")
        tv_gstin.text = intent.getStringExtra("gstin")
    }
}