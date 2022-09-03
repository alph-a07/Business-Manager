package com.example.businessmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_franchisee_dashboard.*
import java.util.*
import kotlin.collections.ArrayList

class FranchiseeDashboardActivity : AppCompatActivity() {
    private val db = Firebase.database
    val list = ArrayList<BusinessForm>()
    lateinit var userBusinesses : ArrayList<BusinessForm>
    lateinit var adapter:AllBusinessesAdapter
    private val currUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_dashboard)

        supportActionBar?.hide()

        iv_franchisee_dashboard_account.setOnClickListener {
            val i = Intent(this, AccountActivity::class.java)
            startActivity(i)
        }

        iv_franchisee_dashboard_search.setOnClickListener {
            val res=ArrayList<BusinessForm>()
            if(editTextTextPersonName.text.isNotEmpty()){
                val key=editTextTextPersonName.text.toString().toLowerCase(Locale.ROOT)
                for(company in list) {
                    if(company.name.toLowerCase(Locale.ROOT).contains(key)){
                        res.add(company)
                    }
                }
                adapter = AllBusinessesAdapter(res,baseContext)
                rv_franchisee_dashboard_list.layoutManager = LinearLayoutManager(baseContext)
                rv_franchisee_dashboard_list.adapter = adapter
            }
            else{
                Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onRestart() {
        super.onRestart()

        list.clear()
       // adapter.clear()
    }
    override fun onStart() {
        super.onStart()

        db.getReference("Users").orderByChild("uid").equalTo(currUser!!.uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                val model = snap.getValue(User::class.java)
                                //userBusinesses= ArrayList<BusinessForm>(model?.listOfBusiness!!)
                                textView2.text = "Hi, " + model!!.userName + " 👋"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            )

        db.getReference("New businesses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val model = snap.getValue(BusinessForm::class.java)
                    list.add(model!!)

                    adapter = AllBusinessesAdapter(list, baseContext)
                    rv_franchisee_dashboard_list.layoutManager = LinearLayoutManager(baseContext)

                    rv_franchisee_dashboard_list.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }
}