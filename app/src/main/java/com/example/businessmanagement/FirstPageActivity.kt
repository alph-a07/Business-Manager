package com.example.businessmanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_first_page.*

class FirstPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_page)

        //to hide default toolbar
        supportActionBar?.hide()

        btn_fp_getStarted.setOnClickListener {
            startActivity(Intent(this,FranchiseeAuthActivity::class.java))
        }


    }
}