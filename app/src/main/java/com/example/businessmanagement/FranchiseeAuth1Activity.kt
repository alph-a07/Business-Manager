package com.example.businessmanagement

import android.content.ContentValues.TAG
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_franchisee_auth1.*


class FranchiseeAuth1Activity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.database

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

        // region LOGIN
        btn_franchisee1_auth_login_button.setOnClickListener {

            // empty number case
            if (edtPhone.text.isEmpty()) {
                edtPhone.error = "Please enter valid number"
                edtPhone.requestFocus()
                return@setOnClickListener
            }
            // empty password case
            if (psswd.text.toString().isEmpty()) {
                psswd.error = "Please enter password"
                psswd.requestFocus()
                return@setOnClickListener
            }
            // verify number and password
            else {
                progress_enter.isVisible = true
                hideKeyboard()

                ccp1.registerCarrierNumberEditText(edtPhone) // register number with country code picker

                // Check whether phone number is already logged in or not
                db.getReference("PhoneUsers").orderByChild("phone")
                    .equalTo(ccp1.fullNumberWithPlus)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // number already exists
                            if (snapshot.exists()) {
                                progress_enter.isVisible = false

                                // verify password
                                for (snap in snapshot.children) {
                                    val model = snap.getValue(User::class.java)
                                    if (model?.phone == ccp1.fullNumberWithPlus) {
                                        if (psswd.text.toString() == model?.password) {
                                            Toast.makeText(
                                                this@FranchiseeAuth1Activity,
                                                "Log in successful!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            updateUI(model.userName)
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
                                    "Phone Number is not registered!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // move to sign up page
                                val intent = Intent(
                                    this@FranchiseeAuth1Activity,
                                    FranchiseeAuth2Activity::class.java
                                )
                                intent.putExtra(
                                    edt_franchisee1_auth_phone.text.toString(),
                                    "phone"
                                ) // !
                                startActivity(intent)
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "Failed to read value.", error.toException())
                        }
                    })

            }
        }
        // endregion

        // region GOOGLE LOGIN
        tv_google.setOnClickListener {
            //progress bar visible
            progress_enter.isVisible = true

            // Configure Google Sign In
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut() // clears cookies about last login and provides all email options each time
            startActivityForResult(googleSignInClient.signInIntent, 1)

        }
        // endregion

        // signUp switch
        tvBtn_franchisee1_auth_signup.setOnClickListener {
            startActivity(Intent(this, FranchiseeAuth2Activity::class.java))
            overridePendingTransition(R.anim.slide_in_right, 0)
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

                    // Check if email is already registered or not
                    db.getReference("GoogleUsers").orderByChild("email")
                        .equalTo(user?.email.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                // email already exists
                                if (snapshot.exists()) {
                                    progress_enter.visibility = View.GONE
                                    Toast.makeText(
                                        this@FranchiseeAuth1Activity,
                                        "Logged in successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // move to dashboard activity
                                    startActivity(
                                        Intent(
                                            this@FranchiseeAuth1Activity,
                                            FranchiseeDashboardActivity::class.java
                                        )
                                    )

                                }

                                // email not registered then move user to signup page
                                else {
                                    progress_enter.visibility = View.GONE
                                    Toast.makeText(
                                        this@FranchiseeAuth1Activity,
                                        "Email is not registered..!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //move to sign up page
                                    startActivity(
                                        Intent(
                                            this@FranchiseeAuth1Activity,
                                            FranchiseeAuth2Activity::class.java
                                        )
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w(TAG, "Failed to read value.", error.toException())
                            }
                        })
                    updateUI(user?.displayName.toString())
                }
                // Google signIn failed
                else {
                    Toast.makeText(this, "Log in failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(name: String) {
        Toast.makeText(this, "Log in successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, FranchiseeDashboardActivity::class.java)
        intent.putExtra(name, "name")
        startActivity(intent)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        var name = ""
        if (currentUser != null) {
            Firebase.database.getReference("PhoneUsers").orderByChild("uid")
                .equalTo(currentUser.uid).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val temp = snapshot.getValue(User::class.java)
                            name = temp!!.userName
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            Firebase.database.getReference("GoogleUsers").orderByChild("uid")
                .equalTo(currentUser.uid).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val temp = snapshot.getValue(User::class.java)
                            name = temp!!.userName
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            updateUI(name)
        }
    }

    private fun hideKeyboard() {
        // Only runs if there is a view that is currently focused
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}