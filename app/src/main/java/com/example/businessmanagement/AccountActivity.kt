package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.businessmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {
    var phoneNum = ""
    var email = ""
    var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.hide()

        card_acc_bookmarks.setOnClickListener {
            //we will show bookmarks list in a new Activity
            val i=Intent(this,BookmarksActivity::class.java)
            startActivity(i)
        }

        card_acc_signout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, FirstPageActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

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
                                    phoneNum = model?.phone.toString()
                                    email = model?.email.toString()
                                    name = model?.userName.toString()

                                    textView5.text = name

                                    if (phoneNum.isEmpty()) {
                                        textView6.text = email
                                    } else
                                        textView6.text = phoneNum

                                    if (model?.accType == "1")
                                        textView7.text = "Franchisee account"
                                    else
                                        textView7.text = "Franchiser account"
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                }
            )
    }
}