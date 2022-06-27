package com.example.businessmanagement

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_first_page.*

class FirstPageActivity : AppCompatActivity() {

    private var franchiseeSwitch = -1;

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_page)

        //to hide default toolbar
        supportActionBar?.hide()

        ll_acc_type_franchisee.setOnClickListener {

            franchiseeSwitch++

            card_acc_type_franchisee.cardElevation = 40F
            card_acc_type_franchisee.outlineAmbientShadowColor = getColor(R.color.Maastricht_Blue)
            card_acc_type_franchisee.outlineSpotShadowColor = getColor(R.color.Maastricht_Blue)

            card_acc_type_franchiser.cardElevation = 20F
            card_acc_type_franchiser.outlineAmbientShadowColor = getColor(R.color.Dim_Gray)
            card_acc_type_franchiser.outlineSpotShadowColor = getColor(R.color.Dim_Gray)

        }

        ll_acc_type_franchiser.setOnClickListener {

            franchiseeSwitch = -1

            card_acc_type_franchisee.cardElevation = 20F
            card_acc_type_franchisee.outlineAmbientShadowColor = getColor(R.color.Dim_Gray)
            card_acc_type_franchisee.outlineSpotShadowColor = getColor(R.color.Dim_Gray)

            card_acc_type_franchiser.cardElevation = 40F
            card_acc_type_franchiser.outlineAmbientShadowColor = getColor(R.color.Maastricht_Blue)
            card_acc_type_franchiser.outlineSpotShadowColor = getColor(R.color.Maastricht_Blue)

        }

        btn_fp_getStarted.setOnClickListener {
            if (franchiseeSwitch >= 0)
                startActivity(Intent(this, FranchiseeAuthActivity::class.java))
            else
                startActivity(Intent(this, FranchiserAuthActivity::class.java))
        }
    }
}