package com.example.covid19

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible

class QuestionnaireActivity : AppCompatActivity() {
    // Initialize Variables
    private lateinit var mainActivityButton: ImageView

    private lateinit var progressBar: ProgressBar
    private lateinit var progressCurrentTextView: TextView
    private lateinit var progressTotalTextView: TextView

    private lateinit var questionTextview: TextView
    private lateinit var descriptionTextView: TextView

    private lateinit var noButton: Button
    private lateinit var yesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show layout
        super.onCreate(savedInstanceState)
        theme.applyStyle(R.style.Theme_dark, true)
        setContentView(R.layout.activity_questionnaire)

        // Get layout components
        mainActivityButton = findViewById(R.id.btn_back_questionnaire)

        progressBar = findViewById(R.id.pb_progress)
        progressCurrentTextView = findViewById(R.id.tv_progress_current)
        progressTotalTextView = findViewById(R.id.tv_progress_total)

        questionTextview = findViewById(R.id.tv_question)
        descriptionTextView = findViewById(R.id.tv_description)

        noButton = findViewById(R.id.btn_no)
        yesButton = findViewById(R.id.btn_yes)

        // Main activity button listener
        mainActivityButton.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        // "No" button listener
        yesButton.setOnClickListener(View.OnClickListener {
            val currentQuestion = Integer.valueOf(progressCurrentTextView.text.toString()) + 1
            if (currentQuestion <= progressBar.max) {
                progressBar.progress = progressBar.progress + 1
                progressCurrentTextView.text = currentQuestion.toString()
            } else {
                progressBar.isVisible = false
            }
        })

        // "Yes" button listener
        noButton.setOnClickListener(View.OnClickListener {
            val currentQuestion = Integer.valueOf(progressCurrentTextView.text.toString()) - 1
            if (currentQuestion > progressBar.min) {
                progressBar.progress = progressBar.progress - 1
                progressCurrentTextView.text = currentQuestion.toString()
            }
        })

        // Remove title bar
        if (supportActionBar != null)
            supportActionBar?.hide()

        // Suppress dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        // HOSPITALIZATION RISK
        // <= 1.2 Close to or lower than average
        // > 1.2 to <= 2	Moderately elevated
        // > 2 to <= 5	Substantially elevated
        // > 5 to <= 10	High
        // > 10	Very High

        // e^(log
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        this.finish()
    }
}