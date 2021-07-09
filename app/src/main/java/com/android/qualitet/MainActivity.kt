package com.android.qualitet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val butReg = findViewById<Button>(R.id.btnReg)
        butReg.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegistrationActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        initFirebase()
    }
}