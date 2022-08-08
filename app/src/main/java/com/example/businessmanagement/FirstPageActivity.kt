package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
            highlightFirstCard(card_acc_type_franchisee, card_acc_type_franchiser)
        }

        ll_acc_type_franchiser.setOnClickListener {
            franchiseeSwitch = -1
            highlightFirstCard(card_acc_type_franchiser, card_acc_type_franchisee)
        }

        btn_fp_getStarted.setOnClickListener {
            if (franchiseeSwitch >= 0)
                startActivity(Intent(this, FranchiseeAuth1Activity::class.java))
            else
                startActivity(Intent(this, FranchiserAuth1Activity::class.java))
        }
        // endregion
    }

    private fun highlightFirstCard(card1: CardView, card2: CardView) {
        card1.cardElevation = 10 * baseContext.resources.displayMetrics.density
        card1.outlineAmbientShadowColor = getColor(R.color.Maastricht_Blue)
        card1.outlineSpotShadowColor = getColor(R.color.Maastricht_Blue)

        card2.cardElevation = 2 * baseContext.resources.displayMetrics.density
        card2.outlineAmbientShadowColor = getColor(R.color.Dim_Gray)
        card2.outlineSpotShadowColor = getColor(R.color.Dim_Gray)
    }

    private fun dimCard(card: CardView) {
        card.cardElevation = 2 * baseContext.resources.displayMetrics.density
        card.outlineAmbientShadowColor = getColor(R.color.Dim_Gray)
        card.outlineSpotShadowColor = getColor(R.color.Dim_Gray)
    }

    // update UI onStart if already logged in
    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            progressBar2.visibility = View.VISIBLE
            textView10.visibility = View.VISIBLE
            iv_fp_logo.visibility = View.GONE
            tv_fp_tagline.visibility = View.GONE
            relativeLayout.visibility = View.GONE
            btn_fp_getStarted.visibility = View.GONE

            Firebase.database.getReference("Users").orderByChild("uid")
                .equalTo(currentUser.uid).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (snap in snapshot.children) {
                                    val temp = snap.getValue(User::class.java)
                                    if (temp!!.accType == "1")
                                        startActivity(
                                            Intent(
                                                this@FirstPageActivity,
                                                FranchiseeDashboardActivity::class.java
                                            )
                                        )
                                    else if (temp.accType == "2")
                                        startActivity(
                                            Intent(
                                                this@FirstPageActivity,
                                                FranchiserDashboardActivity::class.java
                                            )
                                        )
                                }
                            } else {
                                Toast.makeText(this@FirstPageActivity, "F", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    }
                )
        } else {
            Toast.makeText(this@FirstPageActivity, "FF", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRestart() {
        super.onRestart()
        dimCard(card_acc_type_franchisee)
        dimCard(card_acc_type_franchiser)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}