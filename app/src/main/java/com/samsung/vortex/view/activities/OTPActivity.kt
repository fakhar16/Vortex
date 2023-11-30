package com.samsung.vortex.view.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.databinding.ActivityOtpBinding
import com.samsung.vortex.utils.Utils
import java.util.Objects
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private var mAuth: FirebaseAuth? = null
    private var mVerificationId: String? = null

    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Utils.dismissLoadingBar(this@OTPActivity, binding.progressbar.root)
                sendUserToPhoneActivity()
                Toast.makeText(
                    this@OTPActivity,
                    "Invalid phone number, Try again...",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                forceResendingToken: ForceResendingToken
            ) {
                mVerificationId = verificationId
                Utils.dismissLoadingBar(this@OTPActivity, binding.progressbar.root)
                Toast.makeText(
                    this@OTPActivity,
                    "Verification code has been sent",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        binding.btnVerify.setOnClickListener {
            val verificationCode: String =
                Objects.requireNonNull(binding.tvCode.text).toString()
            if (TextUtils.isEmpty(verificationCode)) {
                Toast.makeText(
                    this@OTPActivity,
                    "Please enter verification code",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                binding.progressbar.dialogTitle.text = getString(R.string.VERIFICATION_CODE_TITLE)
                binding.progressbar.dialogDescription.text = getString(R.string.VERIFICATION_CODE_DESCRIPTION)
                Utils.showLoadingBar(this@OTPActivity, binding.progressbar.root)
                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, verificationCode)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val phoneNumber = intent.getStringExtra(getString(R.string.PHONE_NUMBER))
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(phoneNumber!!) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this@OTPActivity) // (optional) Activity for callback binding
            // If no activity is passed, reCAPTCHA verification can not be used.
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding.progressbar.dialogTitle.text = getString(R.string.PHONE_VERIFICATION_TITLE)
        binding.progressbar.dialogDescription.text = getString(R.string.PHONE_VERIFICATION_DESCRIPTION)
        Utils.showLoadingBar(this@OTPActivity, binding.progressbar.root)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Utils.dismissLoadingBar(this@OTPActivity, binding.progressbar.root)
                    sendUserToMainActivity(task.result.user?.phoneNumber)
                } else {
                    Utils.dismissLoadingBar(this@OTPActivity, binding.progressbar.root)
                    val message = task.exception.toString()
                    Toast.makeText(this@OTPActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePhoneNumberInDB(phone: String?) {
        val map: MutableMap<String, Any?> = HashMap()
        map[getString(R.string.UID)] = FirebaseAuth.getInstance().uid
        map[getString(R.string.PHONE_NUMBER)] = phone
        userDatabaseReference.child(FirebaseAuth.getInstance().uid!!)
            .updateChildren(map)
    }

    private fun sendUserToMainActivity(phone: String?) {
        val mainIntent = Intent(this@OTPActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        updatePhoneNumberInDB(phone)
        startActivity(mainIntent)
        finish()
    }

    private fun sendUserToPhoneActivity() {
        val intent = Intent(this@OTPActivity, PhoneLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}