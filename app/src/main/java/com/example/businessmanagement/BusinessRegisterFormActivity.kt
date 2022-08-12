package com.example.businessmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
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
                edtError(edt_form_business_photos) &&
                edtError(edt_form_add_info)
            ) {
                if (textInputLayout.editText!!.text.isNotEmpty() && textInputLayout1.editText!!.text.isNotEmpty()) {
                    ref.child("Users").orderByChild("uid").equalTo(currentUser?.uid.toString())
                        .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (snap in snapshot.children) {
                                        val model = snap.getValue(User::class.java)

                                        val formModel = BusinessForm()
                                        formModel.name = edt_form_business_name.text.toString()
                                        formModel.email = edt_form_business_email.text.toString()
                                        formModel.website =
                                            edt_form_business_website.text.toString()
                                        formModel.address =
                                            edt_form_business_location.text.toString()
                                        formModel.pincode = edt_form_pincode.text.toString()
                                        formModel.gstin = edt_form_gstin.text.toString()
                                        formModel.photoslink =
                                            edt_form_business_photos.text.toString()
                                        formModel.contact =
                                            edt_form_business_contact.text.toString()
                                        formModel.category =
                                            textInputLayout.editText!!.text.toString()
                                        formModel.revenue =
                                            textInputLayout1.editText!!.text.toString()
                                        formModel.description = edt_form_add_info.text.toString()

                                        model!!.list?.add(formModel)

                                        ref.child("Users").child(currentUser?.uid.toString())
                                            .setValue(model)

                                        val businessModel = NewBusinessesList()
                                        businessModel.phone = model.phone
                                        businessModel.email = model.phone
                                        businessModel.info = formModel
                                        ref.child("New businesses")
                                            .child(edt_form_gstin.text.toString())
                                            .setValue(formModel)

                                        startActivity(
                                            Intent(
                                                this@BusinessRegisterFormActivity,
                                                FranchiserDashboardActivity::class.java
                                            )
                                        )
                                        Toast.makeText(
                                            this@BusinessRegisterFormActivity,
                                            "Application submitted.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            }
                        )
                } else if (textInputLayout.editText!!.text.isEmpty()) {
                    textInputLayout.error = "Mandatory field"
                    textInputLayout.requestFocus()
                    return@setOnClickListener
                } else if (textInputLayout1.editText!!.text.isEmpty()) {
                    textInputLayout1.error = "Mandatory field"
                    textInputLayout1.requestFocus()
                    return@setOnClickListener
                }
            }
        }

        // categories drop-down
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

        // revenue drop-down
        val items1 = listOf(
            "less than 5 lacs",
            "5 lacs - 10 lacs",
            "10 lacs - 50 lacs",
            "50 lacs - 1 crore",
            "1 core - 5 crores",
            "5 crores - 10 crores",
            "more than 10 crores"
        )
        val adapter1 = ArrayAdapter(this, R.layout.list_item, items1)
        (textInputLayout1.editText as? AutoCompleteTextView)?.setAdapter(adapter1)
    }

    private fun edtError(edt: EditText): Boolean {
        if (edt.text.isEmpty()) {
            edt.error = "Mandatory field"
            edt.requestFocus()
            return false
        }
        return true
    }

    private fun sendEmail() {
    }
}