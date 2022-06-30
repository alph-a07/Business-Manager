package com.example.businessmanagement

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.businessmanagement.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_franchisee_auth1.progress_enter
import kotlinx.android.synthetic.main.activity_franchisee_auth2.*
import java.util.concurrent.TimeUnit

class FranchiseeAuth2Activity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.database
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_auth2)

        //to hide default toolbar
        supportActionBar?.hide()

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        val edtPhone = findViewById<EditText>(R.id.edt_franchisee2_auth_phone)
        val edtOTP = findViewById<EditText>(R.id.edt_franchisee2_auth_otp)
        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val name = findViewById<EditText>(R.id.edt_franchisee2_auth_name)
        val psswd = findViewById<EditText>(R.id.edt_franchisee1_auth_password)

        //In sign up get otp pressed
        card_OTP_switch2.setOnClickListener {
            //check number
            if (edt_franchisee2_auth_phone.text.isEmpty()) {
                edt_franchisee2_auth_phone.error = "Please enter valid number"
                edt_franchisee2_auth_phone.requestFocus()
            } else { //verify number and send otp

                progress_enter.isVisible = true

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

                            if (e is FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(
                                    baseContext,
                                    "Invalid request",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (e is FirebaseTooManyRequestsException) {
                                Toast.makeText(
                                    baseContext,
                                    "Too many attempts for this number.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            progress_enter.visibility = View.GONE
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            // The SMS verification code has been sent to the provided phone number, we
                            // now need to ask the user to enter the code and then construct a credential
                            // by combining the code with a verification ID.
                            Log.d(ContentValues.TAG, "onCodeSent:$verificationId")

                            // Save verification ID and resending token so we can use them later
                            storedVerificationId = verificationId
                            resendToken = token
                            otp.setText("sent")

                            progress_enter.visibility = View.GONE
                        }
                    }

                ccp.registerCarrierNumberEditText(edtPhone) // register number with country code picker

                // Check if phone number is already logged in before
                db.getReference("PhoneUsers").orderByChild("phone")
                    .equalTo(ccp.fullNumberWithPlus)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // number already exists
                            if (snapshot.exists()) {
                                progress_enter.isVisible = false
                                Toast.makeText(
                                    baseContext,
                                    "Number already exists, Please login with password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //move to login page
                            }

                            // new number login
                            else {
                                if (ccp.isValidFullNumber) {
                                    val options = PhoneAuthOptions.newBuilder(auth)
                                        .setPhoneNumber(ccp.fullNumberWithPlus)       // Phone number to verify
                                        .setTimeout(
                                            60L,
                                            TimeUnit.SECONDS
                                        ) // Timeout and unit
                                        .setActivity(this@FranchiseeAuth2Activity)                 // Activity (for callback binding)
                                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                        .build()
                                    PhoneAuthProvider.verifyPhoneNumber(options)
                                } else {
                                    progress_enter.visibility = View.GONE
                                    Snackbar.make(
                                        it,
                                        "Enter a valid Phone Number!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                            progress_enter.visibility = View.GONE
                        }
                    })

            }
        }

        //sign up btn pressed -- verify otp
        btn_franchisee2_auth_login_button.setOnClickListener {

            progress_enter.visibility = View.VISIBLE
            val otp = edtOTP.text.trim().toString()

            if (name.text.toString().isEmpty()) {
                name.error = "Please enter your name"
                name.requestFocus()
                return@setOnClickListener
            }
            if (psswd.text.toString().isEmpty()) {
                psswd.error = "Please create password"
                psswd.requestFocus()
                return@setOnClickListener
            }

            if (otp.isNotEmpty()) {
                val credential: PhoneAuthCredential =
                    PhoneAuthProvider.getCredential(storedVerificationId, otp)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        progress_enter.visibility = View.GONE
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(ContentValues.TAG, "signInWithCredential:success")
                            val model = User()
                            model.phone = ccp.fullNumberWithPlus
                            model.userName = name.text.toString()
                            model.password = psswd.text.toString()
                            model.uid =
                                FirebaseAuth.getInstance().currentUser?.uid.toString()

                            //uploading in database
                            db.getReference("PhoneUsers").child(auth.uid.toString())
                                .setValue(model)
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                            if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(
                                    this,
                                    task.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                progress_enter.visibility = View.GONE
            }
        }

        tvBtn_franchisee2_auth_login.setOnClickListener {
            startActivity(Intent(this,FranchiseeAuth1Activity::class.java))
        }
    }
}