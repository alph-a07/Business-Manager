package com.example.businessmanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import kotlinx.android.synthetic.main.activity_franchisee_auth.*

class FranchiseeAuthActivity : AppCompatActivity() {

    // true = login
    // false = signup
    private var switch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_franchisee_auth)

        // when signup switch triggered EFFECTIVELY
        tvBtn_franchisee_auth_signup.setOnClickListener {
            if (switch) {
                switch = false;

                // making login switch secondary
                tvBtn_franchisee_auth_login.background =
                    ContextCompat.getDrawable(baseContext, R.drawable.secondary_partition_bg)

                // making signup switch primary
                tvBtn_franchisee_auth_signup.background =
                    ContextCompat.getDrawable(baseContext, R.drawable.primary_partition_bg)

                // OTP field visible for phone verification
                edt_franchisee_auth_otp.visibility = View.VISIBLE
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
            }
        }
    }
}