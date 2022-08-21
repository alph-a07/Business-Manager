package com.example.businessmanagement

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.businessmanagement.model.BusinessForm
import com.example.businessmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_business_details.*

class BusinessDetailsActivity : AppCompatActivity() {

    var account = "0"
    lateinit var business: BusinessForm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_details)

        supportActionBar?.hide()
        ll.bringToFront()

        iv_back_arrow.setOnClickListener {
            finish()
        }

        Handler().postDelayed({
            ll.visibility = View.VISIBLE
        }, 500)

        btn1.setOnClickListener {
            if (account == "1") {
                // bookmark
            } else if (account == "2") {
                val subject =
                    "mailto:itsjeel01@gmail.com?Subject=%5BRequest%20to%20change%20business%20information%5D&Body=This%20mail%20is%20a%20request%20to%20change%20the%20following%20details%20for%20registered%20GSTIN%20number%20" + intent.getStringExtra(
                        "gstin"
                    ) + ".%0A%0ARegistered%20Email%3A%20%3Cemail%3E%0ARegistered%20Phone%3A%20%3Cphone_no%3E%0A%0AUse%20the%20following%20format%20to%20provide%20updated%20data%3A%0A%3Cchange_what%3E%3A%20%3Cchange_to_what%3E%20%20"
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data =
                        Uri.parse(subject)
                }
                try {
                    startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show()
                }
            }
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

        val ref = Firebase.database
        val currUser = FirebaseAuth.getInstance().currentUser

        // to get phone number of current user
        ref.getReference("Users").orderByChild("uid")
            .equalTo(FirebaseAuth.getInstance().currentUser?.uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                val model = snap.getValue(User::class.java)
                                if (model?.uid == currUser?.uid) {
                                    if (model!!.accType == "1") {
                                        btn1.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                baseContext,
                                                R.drawable.bookmarks
                                            )
                                        )
                                        iv.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                baseContext,
                                                R.drawable.otp_logo
                                            )
                                        )
                                        btn2.text = "Request franchise"
                                        account = "1"
                                    } else account = "2"
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            )
    }
}