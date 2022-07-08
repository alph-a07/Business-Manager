package com.example.businessmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BusinessRegisterFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_register_form)

        supportActionBar?.hide()
    }
}