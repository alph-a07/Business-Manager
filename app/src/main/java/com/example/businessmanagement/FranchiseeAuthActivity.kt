package com.example.businessmanagement

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.businessmanagement.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_franchisee_auth.*

class FranchiseeAuthActivity : AppCompatActivity() {

    // true = login
    // false = signup
    private var switch = true
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db=Firebase.database

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
            if(!switch){
                //In sign up get otp pressed
                card_OTP_switch.setOnClickListener {
                    //check number
                    if(edt_franchisee_auth_phone.text.isEmpty()){
                        edt_franchisee_auth_phone.error="Please enter valid number"
                        edt_franchisee_auth_phone.requestFocus()
                    }
                    else{
                        //verify number and send otp

                    }
                }
            }
        }

        // // when login switch triggered EFFECTIVELY
        tvBtn_franchisee_auth_login.setOnClickListener {
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