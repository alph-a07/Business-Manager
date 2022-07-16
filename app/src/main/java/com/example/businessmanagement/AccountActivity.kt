package com.example.businessmanagement

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.businessmanagement.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_account.*
import java.util.concurrent.TimeUnit

class AccountActivity : AppCompatActivity() {


    var phoneNum = ""
    var email = ""
    var name = ""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.hide()

        card_acc_reset.setOnClickListener {

            //make otp edit text and verify button visible
            reset_ll.visibility = View.VISIBLE

            //send otp to registered number
            val callbacks =
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verification without
                        //     user action.
                        Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Log.w(ContentValues.TAG, "onVerificationFailed", e)

                        when (e) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(
                                    baseContext,
                                    "Invalid request",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is FirebaseTooManyRequestsException -> {
                                Toast.makeText(
                                    baseContext,
                                    "Too many attempts for this number.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(baseContext, e.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }

                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.

                        // Save verification ID and resending token so we can use them later
                        storedVerificationId = verificationId
                        resendToken = token
                        Toast.makeText(this@AccountActivity, "Otp sent", Toast.LENGTH_SHORT).show()
                    }
                }

            val options = PhoneAuthOptions.newBuilder(
                FirebaseAuth.getInstance()
            )
                .setPhoneNumber(phoneNum)       // Phone number to verify
                .setTimeout(
                    60L,
                    TimeUnit.SECONDS
                ) // Timeout and unit
                .setActivity(this@AccountActivity)          // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        verify_otp.setOnClickListener {
            hideKeyboard(this)

            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                storedVerificationId,
                edt_otp.text.trim().toString()
            )

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //reset password

                        edt_new_pass1.visibility = View.VISIBLE
                        edt_new_pass2.visibility = View.VISIBLE
                        button.visibility = View.VISIBLE

                    } else {
                        Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        button.setOnClickListener {
            val psw1=edt_new_pass1.text.toString()
            val psw2=edt_new_pass2.text.toString()

            if(psw1==psw2){
                //upload in firebase
                val map=HashMap<String,Any>()
                map["password"] = psw1
                Firebase.database.getReference("Users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).updateChildren(map)
                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                reset_ll.visibility=View.GONE
                edt_new_pass1.isVisible=false
                edt_new_pass2.isVisible=false
                button.isVisible=false
            }
            else{
                edtError(edt_new_pass1)
                edtError(edt_new_pass2)
            }
        }

        card_acc_signout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, FirstPageActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

         val ref = Firebase.database
         val currUser = FirebaseAuth.getInstance().currentUser

        //to get phone number of current user
        ref.getReference("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser?.uid).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        Toast.makeText(this@AccountActivity, "exists", Toast.LENGTH_SHORT).show()
                        for(snap in snapshot.children){
                            val model = snap.getValue(User::class.java)
                            if(model?.uid==currUser?.uid) {
                                phoneNum = model?.phone.toString()
                                email = model?.email.toString()
                                name = model?.userName.toString()

                                textView5.text = name

                                if (phoneNum.isEmpty()) {
                                    textView6.text = email
                                    card_acc_reset.isVisible = false
                                } else
                                    textView6.text = phoneNum
                            }
                        }
                    }
                    else{
                        Toast.makeText(this@AccountActivity, "not exists", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )
    }

    private fun hideKeyboard(activity: Activity) {
        // Only runs if there is a view that is currently focused
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun edtError(edt: EditText){

            edt.error = "Password does not match"
            edt.requestFocus()
    }

}