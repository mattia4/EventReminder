package com.example.eventreminder.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.eventreminder.Constants
import com.example.eventreminder.R
import com.example.eventreminder.databinding.RegisterLoginActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.appcompat.widget.Toolbar

class RegisterLoginActivity : AppCompatActivity() {
    private lateinit var binding: RegisterLoginActivityBinding

    private lateinit var auth: FirebaseAuth

    private var isRegistered = false
    private var toLogin = false
    private var canUserProceed = true

    private var etUserName: EditText? = null
    private var etUserEmail: EditText? = null
    private var etUserPsw: EditText? = null
    private var etUserPswConfirm: EditText? = null

    private var tvToLogin: TextView? = null

    private var llUserName: LinearLayout? = null
    private var llUserEmailPswConfirm: LinearLayout? = null
    private var btnRegisterLogin: Button? = null
    private var pbLoading: ProgressBar? = null
    private var llFrameLoading: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RegisterLoginActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.register_login_activity)
        auth = Firebase.auth

        val toolbar = findViewById<View>(R.id.toolbarAlt) as Toolbar
        setSupportActionBar(toolbar)

        pbLoading = findViewById(R.id.pb_loading_bar)
        llFrameLoading = findViewById(R.id.ll_frame)

        etUserName = findViewById(R.id.et_user_name)
        etUserEmail = findViewById(R.id.et_user_email)
        etUserPsw = findViewById(R.id.et_user_psw)
        etUserPswConfirm = findViewById(R.id.et_user_psw_confirm)
        tvToLogin = findViewById(R.id.to_login)

        llUserName = findViewById(R.id.ll_user_name)
        llUserEmailPswConfirm = findViewById(R.id.ll_user_email_psw_confirm)
        btnRegisterLogin = findViewById(R.id.btn_register_login)

        tvToLogin?.setOnClickListener {
            toLogin = !toLogin
            tvToLogin?.text = if (toLogin) getString(R.string.to_register_fragment) else getString(R.string.to_login_fragment)
            if (toLogin) {
                llUserName?.visibility = View.GONE
                llUserEmailPswConfirm?.visibility = View.GONE
                btnRegisterLogin?.text = "Login"
            } else {
                llUserName?.visibility = View.VISIBLE
                llUserEmailPswConfirm?.visibility = View.VISIBLE
                btnRegisterLogin?.text = "Register"
            }
            clearErrorField()
        }

        btnRegisterLogin?.setOnClickListener {
            if (!toLogin) {
                checkUserPassword(etUserPsw?.text.toString(), etUserPswConfirm?.text.toString())
                checkEmptyField()
                checkUserEmail(etUserEmail?.text.toString())
            }
            if (!canUserProceed) {
                clearField()
                return@setOnClickListener
            }
            if (!isRegistered && !toLogin) {
                firebaseUserRegistration(etUserEmail?.text.toString(), etUserPsw?.text.toString())
            } else {
                if (etUserEmail?.text?.isEmpty() == true || etUserPsw?.text?.isEmpty() == true) {
                    etUserEmail?.error = "Fields must be not null"
                    etUserPsw?.error = "Fields must be not null"
                    return@setOnClickListener
                } else {
                    etUserEmail?.error = null
                    etUserPsw?.error = null
                }
                firebaseUserLogin(etUserEmail?.text.toString(), etUserPsw?.text.toString())
            }
        }
    }

    private fun checkUserEmail(email: String) {
        val charToCheck = "@." // <- inserire i caratteri obbligatori di una email
        for(i in charToCheck ) {
            if(!email.contains(i)) {
                canUserProceed = email.contains(i)
                break
            }
        }
    }

    private fun checkUserName(username: String) {
        if (username.length < 2) {
            canUserProceed = false
            etUserName?.error = "User name cannot be less than 2"
        } else {
            canUserProceed = true
            etUserName?.error = null
        }
    }

    // TODO cambiare con un for
    private fun checkEmptyField() {
        if (etUserName?.text?.isEmpty() == true ||
            etUserEmail?.text?.isEmpty() == true ||
            etUserPsw?.text?.isEmpty() == true ||
            etUserPswConfirm?.text?.isEmpty() == true
        ) {
            etUserName?.error = "Fields must be not null"
            etUserEmail?.error = "Fields must be not null"
            etUserPsw?.error = "Fields must be not null"
            etUserPswConfirm?.error = "Fields must be not null"
            canUserProceed = false
        }
    }

    private fun clearField() {
        etUserName?.text?.clear()
        etUserEmail?.text?.clear()
        etUserPsw?.text?.clear()
        etUserPswConfirm?.text?.clear()
        canUserProceed = true
    }
    private fun clearErrorField() {
        etUserName?.error = null
        etUserEmail?.error = null
        etUserPsw?.error = null
        etUserPswConfirm?.error = null
    }

    private fun checkUserPassword(password: String, confirmedPassword: String) {
        if (password != confirmedPassword) {
            etUserPsw?.error = "Password does not match!"
            etUserPswConfirm?.error = "Password does not match!"
            canUserProceed = false
            return
        } else {
            canUserProceed = true
            etUserPsw?.error = null
            etUserPswConfirm?.error = null
        }
    }

    private fun firebaseUserRegistration(email: String, password: String) {
        pbLoading?.visibility = View.VISIBLE
        val drawable = ResourcesCompat.getDrawable(resources,
            R.drawable.bg_frame_color_loading, null)
        llFrameLoading?.background = drawable

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this, "Registration success.", Toast.LENGTH_SHORT).show()
                val user = auth.currentUser
                //updateUI(user)
                tvToLogin?.performClick()
            } else {
                // If sign in fails, display a message to the user.
                Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
                //updateUI(null)
            }
            llFrameLoading?.setBackgroundDrawable(null)
            pbLoading?.visibility = View.GONE
        }
    }

    private fun firebaseUserLogin(email: String, password: String) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_frame_color_loading, null)
        llFrameLoading?.background = drawable

        pbLoading?.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isRegistered = task.isSuccessful
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithEmail:success")
                    Toast.makeText(this, "signInWithEmail success.", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    // TODO sistemare sotto
                    user?.let {
                        // Name, email address, and profile photo Url
                        val name = user.displayName
                        val email = user.email
                        val photoUrl = user.photoUrl

                        // Check if user's email is verified
                        val emailVerified = user.isEmailVerified

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getToken() instead.
                        val uid = user.uid
                        val i = Intent(this, EventListActivity::class.java)
                        i.putExtra(Constants.USER_UID, uid)
                        startActivity(i)
                    }
                    pbLoading?.visibility = View.GONE
                    llFrameLoading?.background = null

                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                    pbLoading?.visibility = View.GONE
                    llFrameLoading?.background = null
                }
            }
    }
    override fun onBackPressed() {
        val i = Intent(this, RegisterLoginActivity::class.java)
        startActivity(i)
    }
}
