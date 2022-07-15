package com.example.businessmanagement

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    private val ref=Firebase.database.reference
    private val currUser=FirebaseAuth.getInstance().currentUser
    var phoneNum=""
    var email=""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.hide()

        card_acc_reset.setOnClickListener {

            //make otp edit text and verify button visible
            reset_ll.visibility=View.VISIBLE

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

            ref.child("Users").orderByChild("phone")
                .equalTo(phoneNum)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val options = PhoneAuthOptions.newBuilder(
                            FirebaseAuth.getInstance())
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

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })


        }

        // region MOBILE VERIFICATION
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
                        
                    } else {
                        Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        //to get phone number of current user
        ref.child("Users").orderByChild(currUser?.uid.toString()).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val model=snapshot.getValue(User::class.java)
                    phoneNum= model?.phone.toString()
                    email=model?.email.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
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

}