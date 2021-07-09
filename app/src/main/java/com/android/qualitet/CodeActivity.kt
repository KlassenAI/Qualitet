package com.android.qualitet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.lang.Integer.valueOf
import java.util.concurrent.TimeUnit

class CodeActivity : AppCompatActivity() {

    private lateinit var phone: String
    private lateinit var fullPhone: String
    private lateinit var id: String
    private lateinit var editText: EditText
    private lateinit var txtView: TextView
    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (editText.text.length == 6) enterCode()
            editText.clearFocus()
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
                Toast.makeText(applicationContext, "Произошла ошибка, повторите запрос снова", Toast.LENGTH_SHORT).show()
                Log.e("onVerificationCompleted", ex.message.toString())
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)

        supportActionBar?.hide()

        initFirebase()

        if (intent != null) {
            phone = intent.getStringExtra("phone").toString()
            fullPhone = intent.getStringExtra("fullPhone").toString()
            id = intent.getStringExtra("id").toString()
        }

        val txtViewSmsMessage = findViewById<TextView>(R.id.txtViewSmsMessage)
        txtViewSmsMessage.text = resources.getString(R.string.default_sms_message) + " " + "$fullPhone"

        editText = findViewById(R.id.editTextNumber)
        editText.addTextChangedListener(textWatcher)

        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millies: Long) {
                txtView.text =  resources.getString(R.string.default_next_code) + " " + (millies / 1000) + " секунд"
            }

            override fun onFinish() {
                txtView.text = "Нажмите, чтобы повторно отправить СМС с кодом"
                txtView.isEnabled = true
            }
        }
        timer.start()

        txtView = findViewById(R.id.txtViewNextCode)
        txtView.setOnClickListener {
            txtView.isEnabled = false
            timer.start()
            authUser()
        }
    }

    private fun enterCode() {
        val code = editText.text.toString()
        val credential = PhoneAuthProvider.getCredential(id, code)
        AUTH.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(applicationContext, "Регистрация номера $phone прошла успешно", Toast.LENGTH_SHORT).show()
                Log.e("tag", "somt")
            } else {
                if (AUTH.currentUser != null) Toast.makeText(applicationContext, "Вы уже были зарегистрированы", Toast.LENGTH_LONG).show()
                else Toast.makeText(applicationContext, "Произошла ошибка, повторите ввод кода", Toast.LENGTH_LONG).show()
                Log.e("enterCode", it.exception?.message.toString())
            }
        }
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