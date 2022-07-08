package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.businessmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_first_page.*

class FirstPageActivity : AppCompatActivity() {

    private var franchiseeSwitch = -1
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_first_page)

        //to hide default toolbar
        supportActionBar?.hide()

        // region Select account type
        ll_acc_type_franchisee.setOnClickListener {

            franchiseeSwitch++

            card_acc_type_franchisee.cardElevation *= 5
            card_acc_type_franchisee.outlineAmbientShadowColor = getColor(R.color.Maastricht_Blue)
            card_acc_type_franchisee.outlineSpotShadowColor = getColor(R.color.Maastricht_Blue)

            card_acc_type_franchiser.cardElevation /= 5
            card_acc_type_franchiser.outlineAmbientShadowColor = getColor(R.color.Dim_Gray)
            card_acc_type_franchiser.outlineSpotShadowColor = getColor(R.color.Dim_Gray)

        }

        ll_acc_type_franchiser.setOnClickListener {

            franchiseeSwitch = -1

            card_acc_type_franchisee.cardElevation /= 5
            card_acc_type_franchisee.outlineAmbientShadowColor = getColor(R.color.Dim_Gray)
            card_acc_type_franchisee.outlineSpotShadowColor = getColor(R.color.Dim_Gray)

            card_acc_type_franchiser.cardElevation *= 20
            card_acc_type_franchiser.outlineAmbientShadowColor = getColor(R.color.Maastricht_Blue)
            card_acc_type_franchiser.outlineSpotShadowColor = getColor(R.color.Maastricht_Blue)

        }
        // endregion
        btn_fp_getStarted.setOnClickListener {
            if (franchiseeSwitch >= 0)
                startActivity(Intent(this, FranchiseeAuth1Activity::class.java))
            else
                startActivity(Intent(this, FranchiserAuth1Activity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.getReference("PhoneUsers").orderByChild("uid").equalTo(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val model = snapshot.getValue(User::class.java)
                            if (model?.accType == "1")
                                startActivity(
                                    Intent(
                                        this@FirstPageActivity,
                                        FranchiseeDashboardActivity::class.java
                                    )
                                )
                            else
                                startActivity(
                                    Intent(
                                        this@FirstPageActivity,
                                        FranchiserDashboardActivity::class.java
                                    )
                                )
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            db.getReference("GoogleUsers").orderByChild("uid").equalTo(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        db.getReference("GoogleUsers").orderByChild("uid").equalTo(currentUser.uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val model = snapshot.getValue(User::class.java)
                                        if (model?.accType == "1")
                                            startActivity(
                                                Intent(
                                                    this@FirstPageActivity,
                                                    FranchiseeDashboardActivity::class.java
                                                )
                                            )
                                        else
                                            startActivity(
                                                Intent(
                                                    this@FirstPageActivity,
                                                    FranchiserDashboardActivity::class.java
                                                )
                                            )
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}