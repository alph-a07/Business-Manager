package com.example.businessmanagement

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_franchiser_auth1.*
import java.util.concurrent.TimeUnit

class FranchiserAuth1Activity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.database
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var isPhoneNumberVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchiser_auth1)
        //to hide default toolbar
        supportActionBar?.hide()

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        val edtPhone = findViewById<EditText>(R.id.edt_franchisee1_auth_phone)
        ccp3.registerCarrierNumberEditText(edtPhone) // register number with country code picker

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
                    progress_enter3.visibility = View.GONE
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
                    card_OTP_switch3.isEnabled = false
                    card_OTP_switch3.isClickable = false

                    progress_enter3.isVisible = false
                }
            }

        // region MOBILE VERIFICATION
        card_OTP_switch3.setOnClickListener {
            FranchiseeAuth2Activity().hideKeyboard()
            when (otp2.tag) {

                // receive OTP
                "stage1" -> {
                    // check number
                    if (edt_franchiser1_auth_phone.text.isEmpty() || !ccp3.isValidFullNumber) {
                        edt_franchiser1_auth_phone.error = "Please enter valid number"
                        edt_franchiser1_auth_phone.requestFocus()
                    }

                    // verify number and send otp
                    else {
                        progress_enter3.isVisible = true

                        // Check if phone number is already logged in before
                        db.getReference("Users").orderByChild("phone")
                            .equalTo(ccp3.fullNumberWithPlus)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (snapshot.exists()) {
                                        if (ccp3.isValidFullNumber) {
                                            val options = PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber(ccp3.fullNumberWithPlus)       // Phone number to verify
                                                .setTimeout(
                                                    60L,
                                                    TimeUnit.SECONDS
                                                ) // Timeout and unit
                                                .setActivity(this@FranchiserAuth1Activity)                 // Activity (for callback binding)
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
                                    } else {
                                        Toast.makeText(
                                            baseContext,
                                            "Number not found, Please register.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                // verify OTP
                "stage2" -> {
                    progress_enter3.isVisible = true
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        storedVerificationId,
                        edt_franchiser1_auth_otp.text.trim().toString()
                    )
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            otp2.text = "Verified ✅"
                            otp2.tag = "stage3"
                            card_OTP_switch3.isEnabled = false
                            card_OTP_switch3.isClickable = false
                            progress_enter3.isVisible = false
                            edt_franchiser1_auth_otp.isEnabled = false
                            edt_franchiser1_auth_phone.isEnabled = false
                            isPhoneNumberVerified = true
                        } else {
                            Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // OTP TextWatcher
        edt_franchiser1_auth_otp.addTextChangedListener {

            if (edt_franchiser1_auth_otp.text.length == 6) {
                otp2.text = "Verify"
                card_OTP_switch3.isEnabled = true
                card_OTP_switch3.isClickable = true
            }
        }

        // endregion

        // region LOGIN
        btn_franchiser1_auth_login_button.setOnClickListener {

            if (isPhoneNumberVerified) {
                updateUI()
            } else {
                edtPhone.error = "Verify mobile number first"
            }
        }
        // endregion

        // log in with google
        tv_google3.setOnClickListener {
            //progress bar visible
            progress_enter3.isVisible = true

            // Configure Google Sign In
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut() // clears cookies about last login and provides all email options each time
            startActivityForResult(googleSignInClient.signInIntent, 1)

        }

        tvBtn_franchiser1_auth_signup.setOnClickListener {
            startActivity(Intent(this, FranchiserAuth2Activity::class.java))
        }
    }

    private fun updateUI() {
        Toast.makeText(this, "Log in successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, FranchiserDashboardActivity::class.java))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, FirstPageActivity::class.java))
    }

    // must override to capture results from googleSignInClient
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            // task = account info
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                FranchiseeAuth2Activity().firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                progress_enter3.isVisible = false
                Toast.makeText(this, "SignIn failed,try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
}