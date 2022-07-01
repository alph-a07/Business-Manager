package com.example.businessmanagement

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.businessmanagement.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_franchisee_auth1.*

class FranchiseeAuth1Activity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.database
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_auth1)

        //to hide default toolbar
        supportActionBar?.hide()

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        val edtPhone = findViewById<EditText>(R.id.edt_franchisee1_auth_phone)
        val psswd = findViewById<EditText>(R.id.edt_franchisee1_auth_password)

        btn_franchisee1_auth_login_button.setOnClickListener {
            //check number
            if (edtPhone.text.isEmpty()) {
                edtPhone.error = "Please enter valid number"
                edtPhone.requestFocus()
                return@setOnClickListener
            }
            if (psswd.text.toString().isEmpty()) {
                psswd.error = "Please enter password"
                psswd.requestFocus()
                return@setOnClickListener
            } else { //verify number and password

                progress_enter.isVisible = true

                ccp1.registerCarrierNumberEditText(edtPhone) // register number with country code picker

                // Check if phone number is already logged in before
                db.getReference("PhoneUsers").orderByChild("phone")
                    .equalTo(ccp1.fullNumberWithPlus)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // number already exists
                            if (snapshot.exists()) {
                                progress_enter.isVisible = false

                                //verify password
                                for (snap in snapshot.children) {
                                    val model = snap.getValue(User::class.java)
                                    if (model?.phone == ccp1.fullNumberWithPlus) {
                                        if (psswd.text.toString() == model?.password) {
                                            Toast.makeText(
                                                this@FranchiseeAuth1Activity,
                                                "Log in successful",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@FranchiseeAuth1Activity,
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
                                Toast.makeText(
                                    this@FranchiseeAuth1Activity,
                                    "Phone Number is not registered..!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //move to sign up page
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "Failed to read value.", error.toException())
                        }
                    })

            }
        }

        //sign in with google
        ll_franchisee1_google.setOnClickListener {
            //progress bar visible
            progress_enter.isVisible = true

            // Configure Google Sign In
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut() // clears cookies about last login and provides all email options each time
            startActivityForResult(googleSignInClient.signInIntent, 1)

        }

        tvBtn_franchisee1_auth_signup.setOnClickListener {
            startActivity(Intent(this,FranchiseeAuth2Activity::class.java))
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
                progress_enter.isVisible = false
                Toast.makeText(this, "SignIn failed,try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        // user credentials
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progress_enter.isVisible = false

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