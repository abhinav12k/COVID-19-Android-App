package com.example.covid19.data

data class StatesInfectedAndDeathsItem(
        val Confirmed: Int,
        val Date: String,
        val Deaths: Int,
        val Lat: String,
        val Lon: String,
        val Province: String
)