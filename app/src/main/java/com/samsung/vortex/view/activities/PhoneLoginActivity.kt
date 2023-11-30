package com.samsung.vortex.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.samsung.vortex.R
import com.samsung.vortex.databinding.ActivityPhoneLoginBinding

class PhoneLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            if (binding.phoneNumberInput.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra(
                    getString(R.string.PHONE_NUMBER),
                    binding.phoneNumberInput.text.toString()
                )
                startActivity(intent)
            }
        }
    }
}