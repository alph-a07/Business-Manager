package com.example.businessmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    private val ref=Firebase.database.reference
    val currUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        supportActionBar!!.hide()

        //show list in recycler
        //--start
        var list=ArrayList<BusinessForm>()

        //get list of bookmarks from firebase
        ref.child("Users").orderByChild("uid").equalTo(currUser?.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(snap in snapshot.children){
                            val model=snap.getValue(User::class.java)
                            if(model?.uid==currUser?.uid){
                                list= model?.listOfBookmark!!
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        val adapter=AllBusinessesAdapter(list,this)
        rv_bookmark.layoutManager=LinearLayoutManager(this)
        rv_bookmark.adapter=adapter
    }
}