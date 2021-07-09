package com.android.qualitet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import java.util.concurrent.TimeUnit

class RegistrationActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var btnCode: Button
    private lateinit var phone: String
    private lateinit var fullPhone: String
    private lateinit var spinner: Spinner
    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            btnCode.isEnabled = editText.length() > 14
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    override fun onStart() {
        super.onStart()
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                AUTH.signInWithCredential(credential).addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(applicationContext, "Произошла ошибка, повторите запрос снова", Toast.LENGTH_SHORT).show()
                        Log.e("onVerificationCompleted", it.exception?.message.toString())
                    }
                }
            }

            override fun onVerificationFailed(ex: FirebaseException) {
                // Если не была пройдена капча или телефон много раз использовался для тестирования
                Toast.makeText(applicationContext, "Произошла ошибка, повторите запрос снова", Toast.LENGTH_SHORT).show()
                Log.e("onVerificationCompleted", ex.message.toString())
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                val intent = Intent(this@RegistrationActivity, CodeActivity::class.java)
                intent.putExtra("phone", phone)
                intent.putExtra("fullPhone", fullPhone)
                intent.putExtra("id", id)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportActionBar?.hide()

        spinner = findViewById(R.id.spinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, resources.getStringArray(R.array.phone_codes))
        spinner.adapter = adapter

        editText = findViewById(R.id.edit_text)
        val listener = MaskedTextChangedListener("([000]) [000]-[00]-[00]", editText)
        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener
        editText.addTextChangedListener(textWatcher)

        btnCode = findViewById(R.id.btnCode)
        btnCode.setOnClickListener {
            fullPhone = spinner.selectedItem.toString() + editText.text.toString()
            phone = spinner.selectedItem.toString() + editText.text.toString().replace(Regex("[+]|[-]|[(]|[)]|[ ]"), "")
            authUser()
            editText.clearFocus()
        }

        editText.isFocusable = true
    }

    private fun authUser() {
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setActivity(this)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callback)
                .build()
        )
    }
}