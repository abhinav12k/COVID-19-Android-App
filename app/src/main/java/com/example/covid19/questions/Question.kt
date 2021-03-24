package com.example.covid19.questions

data class Question (
    val id: Int,
    val question: String,
    val description: String?,
    var answer: Boolean?
)