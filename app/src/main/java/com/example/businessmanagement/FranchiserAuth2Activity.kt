package com.example.businessmanagement

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.businessmanagement.model.User
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
import kotlinx.android.synthetic.main.activity_franchiser_auth2.*
import java.util.concurrent.TimeUnit

class FranchiserAuth2Activity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.database
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var isPhoneNumberVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchiser_auth2)

        supportActionBar?.hide()

        ccp4.registerCarrierNumberEditText(edt_franchiser2_auth_phone)

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
                    progress_enter4.visibility = View.GONE
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
                    otp3.text = "Code sent"
                    otp3.tag = "stage2"
                    card_OTP_switch3.isEnabled = false
                    card_OTP_switch3.isClickable = false

                    progress_enter4.isVisible = false
                }
            }

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        //google sign up
        tv_google4.setOnClickListener {
            //progress bar visible
            progress_enter4.isVisible = true

            // Configure Google Sign In
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut() // clears cookies about last login and provides all email options each time
            startActivityForResult(googleSignInClient.signInIntent, 1)
        }

        // region MOBILE VERIFICATION
        card_OTP_switch3.setOnClickListener {
            hideKeyboard(this)
            when (otp3.tag) {

                // receive OTP
                "stage1" -> {
                    // check number
                    if (edt_franchiser2_auth_phone.text.isEmpty() || !ccp4.isValidFullNumber) {
                        edt_franchiser2_auth_phone.error = "Please enter valid number"
                        edt_franchiser2_auth_phone.requestFocus()
                    }

                    // verify number and send otp
                    else {
                        progress_enter4.isVisible = true

                        // Check if phone number is already logged in before
                        db.getReference("Users").orderByChild("phone")
                            .equalTo(ccp4.fullNumberWithPlus)
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
                                            this@FranchiserAuth2Activity,
                                            FranchiseeAuth1Activity::class.java
                                        )
                                        intent.putExtra(ccp4.fullNumberWithPlus, String())
                                        startActivity(intent)
                                    }

                                    // new number login
                                    else {
                                        if (ccp4.isValidFullNumber) {
                                            val options = PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber(ccp4.fullNumberWithPlus)       // Phone number to verify
                                                .setTimeout(
                                                    60L,
                                                    TimeUnit.SECONDS
                                                ) // Timeout and unit
                                                .setActivity(this@FranchiserAuth2Activity)                 // Activity (for callback binding)
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
                    progress_enter4.isVisible = true
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        storedVerificationId,
                        edt_franchiser2_auth_otp.text.trim().toString()
                    )

                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            otp3.text = "Verified ✅"
                            otp3.tag = "stage3"
                            card_OTP_switch3.isEnabled = false
                            card_OTP_switch3.isClickable = false
                            progress_enter4.isVisible = false
                            edt_franchiser2_auth_otp.isEnabled = false
                            edt_franchiser2_auth_phone.isEnabled = false
                            isPhoneNumberVerified = true
                        } else {
                            Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // OTP TextWatcher
        edt_franchiser2_auth_otp.addTextChangedListener {

            if (edt_franchiser2_auth_otp.text.length == 6) {
                otp3.text = "Verify"
                card_OTP_switch3.isEnabled = true
                card_OTP_switch3.isClickable = true
            }
        }

        // endregion

        // region SIGN UP
        btn_franchiser2_auth_login_button.setOnClickListener {
            hideKeyboard(this)
            if (isPhoneNumberVerified) {

                progress_enter4.visibility = View.VISIBLE

                if (edt_franchiser2_auth_name?.text.toString().isEmpty()) {
                    edt_franchiser2_auth_name.error = "Enter your name"
                    edt_franchiser2_auth_name.requestFocus()
                    return@setOnClickListener
                } else if (edt_franchiser2_auth_password?.text.toString().isEmpty()) {
                    edt_franchiser2_auth_password.error = "Enter a strong password"
                    edt_franchiser2_auth_password.requestFocus()
                    return@setOnClickListener
                } else {
                    val model = User()
                    model.phone = ccp4.fullNumberWithPlus
                    model.userName = edt_franchiser2_auth_name.text.toString()
                    model.password = edt_franchiser2_auth_password.text.toString()
                    model.accType = "2"
                    model.uid =
                        FirebaseAuth.getInstance().currentUser?.uid.toString()

                    // uploading in database
                    db.getReference("Users").child(auth.uid.toString())
                        .setValue(model)

                    updateUI(model.phone, model.password)
                }
            } else {
                edt_franchiser2_auth_phone.error = "Verify mobile number first."
                edt_franchiser2_auth_phone.requestFocus()
                return@setOnClickListener
            }
        }
        // endregion

        tvBtn_franchiser2_auth_login.setOnClickListener {
            startActivity(Intent(this, FranchiserAuth1Activity::class.java))
        }
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

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                progress_enter4.isVisible = false
                Toast.makeText(this, "SignUp failed,try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this,FranchiserAuth1Activity::class.java))
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        // user credentials
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progress_enter4.isVisible = false

                // Google signIn successful
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val model = User()
                    model.email = user?.email.toString()
                    model.userName = user?.displayName.toString()
                    model.uid = user?.uid.toString()
                    model.accType = "2"
                    db.getReference("Users").child(auth.uid.toString())
                        .setValue(model)

                    startActivity(
                        Intent(
                            this@FranchiserAuth2Activity,
                            FranchiserDashboardActivity::class.java
                        )
                    )
                }
                // Google signIn failed
                else {
                    Toast.makeText(this, "Sign up failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(phone: String, password: String) {
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, FranchiserAuth1Activity::class.java)
        intent.putExtra(phone,"phone")
        intent.putExtra(password,"password")
        startActivity(intent)
    }

    private fun hideKeyboard(activity: Activity) {
        // Only runs if there is a view that is currently focused
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}