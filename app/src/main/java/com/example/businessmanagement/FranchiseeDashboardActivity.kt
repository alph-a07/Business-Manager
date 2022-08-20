package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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

class FranchiseeDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_dashboard)

        supportActionBar?.hide()

        iv_franchisee_dashboard_account.setOnClickListener {
            val i = Intent(this, AccountActivity::class.java)
            startActivity(i)
        }

        iv_franchisee_dashboard_filter.setOnClickListener {
            if (rl_filters.isVisible) rl_filters.visibility = View.GONE
            else rl_filters.visibility = View.VISIBLE
        }

        btn_apply_filters.setOnClickListener {
            rl_filters.visibility = View.GONE
            val category = categoryTextInputLayout.editText!!.text
            val revenue = revenueTextInputLayout.editText!!.text

            val filteredList = ArrayList<BusinessForm>()
            Firebase.database.getReference("New businesses")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (snap in snapshot.children) {
                            val model = snap.getValue(BusinessForm::class.java)
                            if (!category.equals("Select Category") && !revenue.equals("Choose Business Worth") && model!!.category.equals(
                                    category
                                ) && model.revenue.equals(revenue)
                            )
                                filteredList.add(model)
                            else if (!category.equals("Select Category") && revenue.equals("Choose Business Worth") && model!!.category.equals(
                                    category
                                )
                            )
                                filteredList.add(model)
                            else if (model!!.revenue.equals(revenue))
                                filteredList.add(model)

                            val adapter = AllBusinessesAdapter(filteredList, baseContext)
                            rv_franchisee_dashboard_list.layoutManager =
                                LinearLayoutManager(baseContext)
                            rv_franchisee_dashboard_list.adapter = adapter
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onStart() {
        super.onStart()
        val db = Firebase.database
        val list = ArrayList<BusinessForm>()

        db.getReference("New businesses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val model = snap.getValue(BusinessForm::class.java)
                    list.add(model!!)

                    val adapter = AllBusinessesAdapter(list, baseContext)
                    rv_franchisee_dashboard_list.layoutManager = LinearLayoutManager(baseContext)
                    rv_franchisee_dashboard_list.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val currUser = FirebaseAuth.getInstance().currentUser

        db.getReference("Users").orderByChild("uid").equalTo(currUser!!.uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                val model = snap.getValue(User::class.java)
                                textView2.text = "Hi, " + model!!.userName + " 👋"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            )
    }
}