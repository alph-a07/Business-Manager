package com.example.businessmanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class FranchisorAuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisor_auth)
        //to hide default toolbar
        supportActionBar?.hide()
    }
}