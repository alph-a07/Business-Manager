package com.example.businessmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.hide()
    }
}