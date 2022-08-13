package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businessmanagement.adapter.AllBusinessesAdapter
import com.example.businessmanagement.model.BusinessForm
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_franchisee_dashboard.*

class FranchiseeDashboardActivity : AppCompatActivity() {

    private val db=Firebase.database
    var list=ArrayList<BusinessForm>()
    private var bookmarksList=ArrayList<BusinessForm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_dashboard)

        supportActionBar?.hide()

        iv_franchisee_dashboard_account.setOnClickListener {
            val i=Intent(this,AccountActivity::class.java)
            i.putExtra("bookmarks",bookmarksList)
            startActivity(i)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onStart() {
        super.onStart()
        //to show the all business in recycler view
        //--start

        //get all data from firebase
        db.reference.child("New businesses").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val model=snap.getValue(BusinessForm::class.java)
                    list.add(model!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        Log.e("list",list.toString())

        val adapter=AllBusinessesAdapter(list,bookmarksList,this)
        rv_franchisee_dashboard_list.layoutManager=LinearLayoutManager(this)
        rv_franchisee_dashboard_list.adapter=adapter

        //--end
    }
}