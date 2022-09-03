package com.example.businessmanagement

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businessmanagement.adapter.AllBusinessesAdapter
import com.example.businessmanagement.model.BusinessForm
import com.example.businessmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_bookmarks.*

class BookmarksActivity : AppCompatActivity() {
    private val db=Firebase.database
    private val currUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        supportActionBar!!.hide()

        //show list in recycler
        //--start
        val list=ArrayList<BusinessForm>()

        //get list of bookmarks from firebase
        db.getReference("Users").orderByChild("uid").equalTo(currUser?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val model = snap.getValue(User::class.java)

                            for(x in model!!.listOfBusiness!!){
                                if(x.isBookmarked){
                                    list.add(x)
                                    val adapter=AllBusinessesAdapter(list,baseContext)
                                    rv_bookmark.layoutManager=LinearLayoutManager(baseContext)
                                    rv_bookmark.adapter=adapter
                                }
                            }
                            Log.d("bookmarks",list.toString())
                        }
                    }
                    else{
                        Toast.makeText(this@BookmarksActivity, "not exists", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })


    }
}