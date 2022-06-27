package com.example.businessmanagement

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_franchisee_auth.*
import java.util.concurrent.TimeUnit

class FranchiseeAuthActivity : AppCompatActivity() {

    // true = login
    // false = signup
    private var switch = true
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db=Firebase.database
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_auth)

        //to hide default toolbar
        supportActionBar?.hide()

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        //login process
        if(switch){
            progress_enter.visibility = View.GONE
            val edtPhone = findViewById<EditText>(R.id.edt_franchisee_auth_phone)
            val psswd=findViewById<EditText>(R.id.edt_franchisee_auth_password)

            btn_franchisee_auth_login_button.setOnClickListener {
                //check number
                if(switch&&edtPhone.text.isEmpty()){
                    edtPhone.error="Please enter valid number"
                    edtPhone.requestFocus()
                    return@setOnClickListener
                }
                if(switch&&psswd.text.toString().isEmpty()){
                    psswd.error="Please enter password"
                    psswd.requestFocus()
                    return@setOnClickListener
                }
                else { //verify number and password

                    progress_enter.isVisible=true

                    ccp.registerCarrierNumberEditText(edtPhone) // register number with country code picker

                    // Check if phone number is already logged in before
                    db.getReference("PhoneUsers").orderByChild("phone")
                        .equalTo(ccp.fullNumberWithPlus)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                // number already exists
                                if (snapshot.exists()) {
                                    progress_enter.isVisible = false

                                    //verify password
                                    for (snap in snapshot.children) {
                                        val model = snap.getValue(User::class.java)
                                        if (model?.phone == ccp.fullNumberWithPlus) {
                                            if(psswd.text.toString() == model?.password){
                                                Toast.makeText(this@FranchiseeAuthActivity,
                                                    "Log in successful",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }else{
                                                Toast.makeText(this@FranchiseeAuthActivity,
                                                    "Incorrect password",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }

                                // number not registered then move user to signup page
                                else {
                                    progress_enter.visibility = View.GONE
                                    Toast.makeText(this@FranchiseeAuthActivity,
                                        "Phone Number is not registered..!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //move to sign up page
                                    signUpPage()
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w(TAG, "Failed to read value.", error.toException())
                            }
                        })

                }
            }
        }

        //sign in with google
        ll_franchisee_google.setOnClickListener {
            //progress bar visible
            progress_enter.isVisible=true

            // Configure Google Sign In
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut() // clears cookies about last login and provides all email options each time
            startActivityForResult(googleSignInClient.signInIntent, 1)

        }

        // when signup switch triggered EFFECTIVELY
        tvBtn_franchisee_auth_signup.setOnClickListener {

            //move to signup page
            signUpPage()

            if(!switch){
                progress_enter.visibility = View.GONE
                val edtPhone = findViewById<EditText>(R.id.edt_franchisee_auth_phone)
                val edtOTP = findViewById<EditText>(R.id.edt_franchisee_auth_otp)
                val ccp = findViewById<CountryCodePicker>(R.id.ccp)
                val name = findViewById<EditText>(R.id.edt_franchisee_auth_name)
                val psswd=findViewById<EditText>(R.id.edt_franchisee_auth_password)

                //In sign up get otp pressed
                card_OTP_switch.setOnClickListener {
                    //check number
                    if(!switch && edt_franchisee_auth_phone.text.isEmpty()){
                        edt_franchisee_auth_phone.error="Please enter valid number"
                        edt_franchisee_auth_phone.requestFocus()
                    }
                    else { //verify number and send otp

                        progress_enter.isVisible=true

                        val callbacks =
                            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    // This callback will be invoked in two situations:
                                    // 1 - Instant verification. In some cases the phone number can be instantly
                                    //     verified without needing to send or enter a verification code.
                                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                                    //     detect the incoming verification SMS and perform verification without
                                    //     user action.
                                    Log.d(TAG, "onVerificationCompleted:$credential")

                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    // This callback is invoked in an invalid request for verification is made,
                                    // for instance if the the phone number format is not valid.
                                    Log.w(TAG, "onVerificationFailed", e)

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
                                    Log.d(TAG, "onCodeSent:$verificationId")

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
                                        progress_enter.isVisible=false
                                        Toast.makeText(
                                            baseContext,
                                            "Number already exists, Please login with password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //move to login page
                                        loginPage()
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
                                                .setActivity(this@FranchiseeAuthActivity)                 // Activity (for callback binding)
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
                                    Log.w(TAG, "Failed to read value.", error.toException())
                                    progress_enter.visibility = View.GONE
                                }
                            })

                    }
                }

                //sign up btn pressed -- verify otp
                btn_franchisee_auth_login_button.setOnClickListener{

                    progress_enter.visibility = View.VISIBLE
                    val otp = edtOTP.text.trim().toString()

                    if(!switch && name.text.toString().isEmpty()){
                        name.error="Please enter your name"
                        name.requestFocus()
                        return@setOnClickListener
                    }
                    if(!switch && psswd.text.toString().isEmpty()){
                        psswd.error="Please create password"
                        psswd.requestFocus()
                        return@setOnClickListener
                    }

                    if (!switch && otp.isNotEmpty()) {
                        val credential: PhoneAuthCredential =
                            PhoneAuthProvider.getCredential(storedVerificationId, otp)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener(this) { task ->
                                progress_enter.visibility = View.GONE
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success")
                                    val model = User()
                                    model.phone = ccp.fullNumberWithPlus
                                    model.userName = name.text.toString()
                                    model.password=psswd.text.toString()
                                    model.uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

                                    //uploading in database
                                    db.getReference("PhoneUsers").child(auth.uid.toString()).setValue(model)
                                    updateUI()
                                } else {
                                    // Sign in failed, display a message and update the UI
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
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
                    }
                    else {
                        Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                        progress_enter.visibility = View.GONE
                    }
                }
            }

        }

        // when login switch triggered EFFECTIVELY
        tvBtn_franchisee_auth_login.setOnClickListener {
            loginPage()
        }
    }

    fun loginPage(){
        if (!switch){

            switch = true

            // making signup switch secondary
            tvBtn_franchisee_auth_signup.background =
                ContextCompat.getDrawable(baseContext, R.drawable.secondary_partition_bg)

            // making login switch primary
            tvBtn_franchisee_auth_login.background =
                ContextCompat.getDrawable(baseContext, R.drawable.primary_partition_bg)

            // OTP field gone
            ll_franchisee_auth_otp.visibility = View.GONE
            card_OTP_switch.visibility = View.GONE

            // Name field gone
            ll_franchisee_auth_name.visibility = View.GONE

            // adjust password field to "enter password" from "create password"
            edt_franchisee_auth_password.tag = "login_password"
            edt_franchisee_auth_password.hint = "Enter password"

            // updating "signup" button "login" button
            btn_franchisee_auth_login_button.tag = "login"
            "Login".also { btn_franchisee_auth_login_button.text = it }
        }
    }

    fun signUpPage(){
        if (switch) {
            switch = false

            // making login switch secondary
            tvBtn_franchisee_auth_login.background =
                ContextCompat.getDrawable(baseContext, R.drawable.secondary_partition_bg)

            // making signup switch primary
            tvBtn_franchisee_auth_signup.background =
                ContextCompat.getDrawable(baseContext, R.drawable.primary_partition_bg)

            // OTP field visible for phone verification
            ll_franchisee_auth_otp.visibility = View.VISIBLE
            card_OTP_switch.visibility = View.VISIBLE

            // name field visible
            ll_franchisee_auth_name.visibility = View.VISIBLE

            // adjust password field to "create password" from "enter password"
            edt_franchisee_auth_password.tag = "signup_password"
            edt_franchisee_auth_password.hint = "Create password"
            tvBtn_franchisee_auth_forgot_password.visibility = View.GONE

            // updating "login" button "signup" button
            btn_franchisee_auth_login_button.tag = "signup"
            "Sign Up".also { btn_franchisee_auth_login_button.text = it }
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
            }
            catch (e: ApiException) {
                progress_enter.isVisible=false
                Toast.makeText(this, "SignIn failed,try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        // user credentials
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progress_enter.isVisible=false

                // Google signIn successful
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val model = User()
                    model.email = user?.email.toString()
                    model.userName = user?.displayName.toString()
                    model.uid = user?.uid.toString()
                    db.getReference("GoogleUsers").child(auth.uid.toString())
                        .setValue(model)

                    updateUI()
                }
                // Google signIn failed
                else {
                    Toast.makeText(this, "Sign in failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI() {
        Toast.makeText(this, "Sign in successfully", Toast.LENGTH_SHORT).show()
        //startActivity(Intent(this, MainActivity::class.java))
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //reload();
            //startActivity(Intent(this, MainActivity::class.java))
        }
    }
}