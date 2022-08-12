package com.example.businessmanagement.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.businessmanagement.BusinessDetailsActivity
import com.example.businessmanagement.R
import com.example.businessmanagement.model.BusinessForm

class RegisteredBusinessesAdapter(private val list:ArrayList<BusinessForm>,private val context: Context): RecyclerView.Adapter<RegisteredBusinessesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int):ViewHolder {
        val sampleViews = LayoutInflater.from(parent.context).inflate(R.layout.sample_registered_businesses,parent,false)
        return ViewHolder(sampleViews)
    }

    override fun onBindViewHolder(holder:ViewHolder,position:Int) {
        val model:BusinessForm = list[position]

        holder.name.text = model.name
        holder.category.text = model.category
        holder.logo.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.bg))

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context,BusinessDetailsActivity::class.java)
            intent.putExtra("name",model.name)
            intent.putExtra("category",model.category)
            intent.putExtra("revenue",model.revenue)
            intent.putExtra("description",model.description)
            intent.putExtra("website",model.website)
            intent.putExtra("address",model.address + " - " + model.pincode)
            intent.putExtra("mail",model.email)
            intent.putExtra("phone",model.contact)
            intent.putExtra("gstin",model.gstin)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount():Int {
        return list.size
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_registered_business_name)
        val category: TextView = itemView.findViewById(R.id.tv_registered_business_category)
        val logo:ImageView = itemView.findViewById(R.id.iv_registered_business_logo)
    }
}
