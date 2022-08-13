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
import kotlinx.android.synthetic.main.sample_business_card.view.*

class AllBusinessesAdapter(private val list:ArrayList<BusinessForm>,
                           private var bookmarksList: ArrayList<BusinessForm>,
                           private val context: Context): RecyclerView.Adapter<AllBusinessesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): AllBusinessesAdapter.ViewHolder {
        val sampleViews = LayoutInflater.from(parent.context).inflate(R.layout.sample_business_card,parent,false)
        return AllBusinessesAdapter.ViewHolder(sampleViews)
    }

    override fun getItemCount():Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AllBusinessesAdapter.ViewHolder, position: Int) {
        val model: BusinessForm = list[position]

        holder.name.text = model.name
        holder.worth.text = model.revenue
        holder.category.text = model.category
        holder.logo.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.bg))

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, BusinessDetailsActivity::class.java)
            it.context.startActivity(intent)
        }
        holder.bookmark.setOnClickListener {
            //set bookmarked
            if (holder.bookmark.drawable==ContextCompat.getDrawable(context,R.drawable.bookmarks)){
                holder.bookmark.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_bookmark_24))
                //add in the bookmarks
                bookmarksList.add(model)
            }
            //if already bookmarked then remove it
            else{
                holder.bookmark.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.bookmarks))
                //remove it
                bookmarksList.remove(model)
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