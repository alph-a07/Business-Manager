package com.example.businessmanagement

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
import kotlinx.android.synthetic.main.activity_franchisee_auth2.*
import java.util.concurrent.TimeUnit

class FranchiseeAuth2Activity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.database
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var isPhoneNumberVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_auth2)

        // to hide default toolbar
        supportActionBar?.hide()

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        ccp2.registerCarrierNumberEditText(edt_franchisee2_auth_phone)

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
                    progress_enter2.visibility = View.GONE
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
                    otp2.text = "Code sent"
                    otp2.tag = "stage2"
                    card_OTP_switch2.isEnabled = false
                    card_OTP_switch2.isClickable = false

                    progress_enter2.isVisible = false
                }
            }

        // region MOBILE VERIFICATION
        card_OTP_switch2.setOnClickListener {

            when (otp2.tag) {

                // receive OTP
                "stage1" -> {
                    // check number
                    if (edt_franchisee2_auth_phone.text.isEmpty() || !ccp2.isValidFullNumber) {
                        edt_franchisee2_auth_phone.error = "Please enter valid number"
                        edt_franchisee2_auth_phone.requestFocus()
                    }

                    // verify number and send otp
                    else {
                        progress_enter2.isVisible = true

                        // Check if phone number is already logged in before
                        db.getReference("PhoneUsers").orderByChild("phone")
                            .equalTo(ccp2.fullNumberWithPlus)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    // number already exists
                                    if (snapshot.exists()) {
                                        Toast.makeText(
                                            baseContext,
                                            "Number already exists, please login",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        val intent = Intent(
                                            this@FranchiseeAuth2Activity,
                                            FranchiseeAuth1Activity::class.java
                                        )
                                        intent.putExtra(ccp2.fullNumberWithPlus, String())
                                        startActivity(intent)
                                    }

                                    // new number login
                                    else {
                                        if (ccp2.isValidFullNumber) {
                                            val options = PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber(ccp2.fullNumberWithPlus)       // Phone number to verify
                                                .setTimeout(
                                                    60L,
                                                    TimeUnit.SECONDS
                                                ) // Timeout and unit
                                                .setActivity(this@FranchiseeAuth2Activity)                 // Activity (for callback binding)
                                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                                .build()
                                            PhoneAuthProvider.verifyPhoneNumber(options)
                                        } else {
                                            Snackbar.make(
                                                it,
                                                "Enter a valid Phone Number!",
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }
                }

                // verify OTP
                "stage2" -> {
                    progress_enter2.isVisible = true
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        storedVerificationId,
                        edt_franchisee2_auth_otp.text.trim().toString()
                    )

                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            otp2.text = "Verified ✅"
                            otp2.tag = "stage3"
                            card_OTP_switch2.isEnabled = false
                            card_OTP_switch2.isClickable = false
                            progress_enter2.isVisible = false
                            edt_franchisee2_auth_otp.isEnabled = false
                            edt_franchisee2_auth_phone.isEnabled = false
                            isPhoneNumberVerified = true
                        } else {
                            Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // OTP TextWatcher
        edt_franchisee2_auth_otp.addTextChangedListener {
            if (edt_franchisee2_auth_otp.text.length == 6) {
                otp2.text = "Verify"
                card_OTP_switch2.isEnabled = true
                card_OTP_switch2.isClickable = true
            }
        }

        // endregion

        // region SIGN UP
        btn_franchisee2_auth_login_button.setOnClickListener {
            if (isPhoneNumberVerified) {

                progress_enter2.visibility = View.VISIBLE

                if (edt_franchisee2_auth_name?.text.toString().isEmpty()) {
                    edt_franchisee2_auth_name.error = "Enter your name"
                    edt_franchisee2_auth_name.requestFocus()
                    return@setOnClickListener
                } else if (edt_franchisee2_auth_password?.text.toString().isEmpty()) {
                    edt_franchisee2_auth_password.error = "Enter a strong password"
                    edt_franchisee2_auth_password.requestFocus()
                    return@setOnClickListener
                } else {
                    val model = User()
                    model.phone = ccp2.fullNumberWithPlus
                    model.userName = edt_franchisee2_auth_name.text.toString()
                    model.password = edt_franchisee2_auth_password.text.toString()
                    model.uid =
                        FirebaseAuth.getInstance().currentUser?.uid.toString()

                    // uploading in database
                    db.getReference("PhoneUsers").child(auth.uid.toString())
                        .setValue(model)

                    val intent = Intent(
                        this@FranchiseeAuth2Activity,
                        FranchiseeAuth1Activity::class.java
                    )
                    intent.putExtra(model.phone, String())
                    intent.putExtra(model.password, String())
                    startActivity(intent)
                }
            } else {
                edt_franchisee2_auth_phone.error = "Verify mobile number first."
                edt_franchisee2_auth_phone.requestFocus()
                return@setOnClickListener
            }
        }
        // endregion

        tvBtn_franchisee2_auth_login.setOnClickListener {
            startActivity(Intent(this, FranchiseeAuth1Activity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}