package com.example.covid19

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate

class QuestionnaireActivity : AppCompatActivity() {

    lateinit var mainActivityButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)

        mainActivityButton = findViewById(R.id.back_from_questionnaire)

        mainActivityButton.setOnClickListener(View.OnClickListener {
           onBackPressed()
        })

        // Remove title bar
        if (supportActionBar != null)
            supportActionBar?.hide()

        // Suppress Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        this.overridePendingTransition(0, 0);
        this.finish()
    }
}