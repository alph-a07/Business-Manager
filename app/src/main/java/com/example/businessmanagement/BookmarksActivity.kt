package com.example.businessmanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import com.example.businessmanagement.adapter.AllBusinessesAdapter
import com.example.businessmanagement.model.BusinessForm

class BookmarksActivity(list:ArrayList<BusinessForm>) : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        //show list in recycler
        //--start

        //get list of bookmarks from intent
        //val list= Intent().getParcelableArrayListExtra<>("bookmarks")
        //val adapter=AllBusinessesAdapter(list,ArrayList<BusinessForm>(),this)
    }
}