package com.example.covid19.questions

import java.util.*
import kotlin.collections.ArrayList

object Constants{

    fun getQuestions(): ArrayList<Question>{
        val questionList = ArrayList<Question>()

        val q1 = Question(
            1,
            "Have you tested positive for COVID-19?",
            "Either you are currently positive or had tested positive in the past.",
            null)
        questionList.add(q1)

        val q2 = Question(
            2,
            "Do you have some Pre-Condition such as:",
            "Diabetes, Asthma, Cardiac Arrhythmia, etc",
            null)
        questionList.add(q2)

        return questionList
    }
}