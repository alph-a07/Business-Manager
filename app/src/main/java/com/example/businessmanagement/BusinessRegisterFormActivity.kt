package com.example.businessmanagement

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.businessmanagement.model.BusinessForm
import com.example.businessmanagement.model.NewBusinessesList
import com.example.businessmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_business_register_form.*

class BusinessRegisterFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_register_form)
        val ref = Firebase.database.reference
        val currentUser = FirebaseAuth.getInstance().currentUser

        supportActionBar?.hide()

        btn_form_request_approval.setOnClickListener {
            if (edtError(edt_form_business_name) &&
                edtError(edt_form_business_website) &&
                edtError(edt_form_business_location) &&
                edtError(edt_form_pincode) &&
                edtError(edt_form_business_email) &&
                edtError(edt_form_business_contact) &&
                edtError(edt_form_gstin) &&
                edtError(edt_form_business_photos)
            ) {
                ref.child("Users").orderByChild(currentUser?.uid.toString())
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val model = snapshot.getValue(User::class.java)

                                val formModel = BusinessForm()
                                formModel.name = edt_form_business_name.text.toString()
                                formModel.email = edt_form_business_email.text.toString()
                                formModel.website = edt_form_business_website.text.toString()
                                formModel.address = edt_form_business_location.text.toString()
                                formModel.pincode = edt_form_pincode.text.toString()
                                formModel.gstin = edt_form_gstin.text.toString()
                                formModel.photoslink = edt_form_business_photos.text.toString()
                                formModel.contact = edt_form_business_contact.text.toString()

                                model?.list?.add(formModel)
                                ref.child("Users").child(currentUser?.uid.toString())
                                    .setValue(model)

                                val businessModel = NewBusinessesList()
                                businessModel.phone = model!!.phone
                                businessModel.email = model.phone
                                businessModel.info = formModel
                                ref.child("New businesses").child(edt_form_gstin.text.toString())
                                    .setValue(formModel)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        }
                    )
            }
        }

        val items = listOf(
            "Automotive",
            "Business Support & Supplies",
            "Computers & Electronics",
            "Construction & Contractors",
            "Education",
            "Entertainment",
            "Food & Dining",
            "Health & Medicine",
            "Home & Garden",
            "Legal & Financial",
            "Merchants (Wholesale)",
            "Merchants (Retail)",
            "Personal Care & Services",
            "Real Estate",
            "Travel & Transportation"
        )
        val adapter = ArrayAdapter(this, R.layout.list_item, items)
        (textInputLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun edtError(edt: EditText): Boolean {
        if (edt.text.isEmpty()) {
            edt.error = "Mandatory field"
            edt.requestFocus()
            return false
        }
        return true
    }
}