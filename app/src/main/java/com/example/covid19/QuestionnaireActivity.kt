package com.example.covid19

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.covid19.questions.Constants
import com.example.covid19.questions.Question
import kotlinx.android.synthetic.main.activity_questionnaire.*
import kotlin.math.log

class QuestionnaireActivity : AppCompatActivity() {

    lateinit var mainActivityButton : ImageView

    private var mCurrentPosition : Int = 1
    private var mQuestionsList : ArrayList<Question>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.applyStyle(R.style.Theme_dark, true)
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

        // Get Questions
        mQuestionsList = Constants.getQuestions()
        mCurrentPosition = 1
        setQuestion()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        this.overridePendingTransition(0, 0);
        this.finish()
    }

    private fun setQuestion(){
        val question = mQuestionsList!![mCurrentPosition - 1]

        // Progress Bar
        questionProgressBar.progress = mCurrentPosition
        questionProgress.text = mCurrentPosition.toString()
        questionTotal.text = "/" + questionProgressBar.max

        // Card Info
        questionTitle.text = question.question
        questionDescription.text = question.description
    }

    /** Called when the user touches a Yes/No button  */
    fun clickedButton(view: View?) {
        if (mCurrentPosition - 1 < 2){
            mQuestionsList?.get(mCurrentPosition - 1)?.answer = view.toString().contains("yesButton")
            //Log.i("joseTest", mCurrentPosition.toString())
            mCurrentPosition+=1
            setQuestion()
        } else {
            // Change to multiple choices question (last one)
        }
    }
}