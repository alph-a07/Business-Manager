package com.example.businessmanagement.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.businessmanagement.BusinessDetailsActivity
import com.example.businessmanagement.R
import com.example.businessmanagement.model.BusinessForm
import com.example.businessmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AllBusinessesAdapter(
    private val list: ArrayList<BusinessForm>,
    private val context: Context
) : RecyclerView.Adapter<AllBusinessesAdapter.ViewHolder>() {

    private val db = Firebase.database
    val currUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val sampleViews = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_business_card, parent, false)
        return ViewHolder(sampleViews)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: BusinessForm = list[position]

        holder.name.text = model.name
        holder.worth.text = model.revenue
        holder.category.text = model.category
        holder.logo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bg))

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, BusinessDetailsActivity::class.java)
            intent.putExtra("name", model.name)
            intent.putExtra("category", model.category)
            intent.putExtra("revenue", model.revenue)
            intent.putExtra("description", model.description)
            intent.putExtra("website", model.website)
            intent.putExtra("address", model.address + " - " + model.pincode)
            intent.putExtra("mail", model.email)
            intent.putExtra("phone", model.contact)
            intent.putExtra("gstin", model.gstin)
            it.context.startActivity(intent)
        }

        holder.bookmark.setOnClickListener {
            if (holder.bookmark.tag == "0") {
                db.getReference("Users").orderByChild("uid").equalTo(currUser?.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (snap in snapshot.children) {
                                    val model1 = snap.getValue(User::class.java)

                                    if (!model1!!.listOfBookmark!!.contains(model))
                                        model1.listOfBookmark!!.add(model)

                                    db.reference.child("Users").child(currUser?.uid.toString())
                                        .setValue(model1)

                                    holder.bookmark.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_baseline_bookmark_24
                                        )
                                    )
                                    holder.bookmark.tag = "1"
                                    Toast.makeText(
                                        context,
                                        "Added",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            } else {
                db.getReference("Users").orderByChild("uid").equalTo(currUser?.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (snap in snapshot.children) {
                                    val model1 = snap.getValue(User::class.java)

                                    while (model1!!.listOfBookmark!!.contains(model))
                                        model1.listOfBookmark!!.remove(model)

                                    db.reference.child("Users").child(currUser?.uid.toString())
                                        .setValue(model1)
                                    Toast.makeText(
                                        context,
                                        "Removed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    holder.bookmark.tag = "0"
                                    holder.bookmark.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.bookmarks
                                        )
                                    )
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_sample_business_name)
        val worth: TextView = itemView.findViewById(R.id.tv_sample_business_worth)
        val category: TextView = itemView.findViewById(R.id.tv_sample_business_category)
        val logo: ImageView = itemView.findViewById(R.id.iv_sample_business_logo)
        val bookmark: ImageView = itemView.findViewById(R.id.imageView2)
    }


}