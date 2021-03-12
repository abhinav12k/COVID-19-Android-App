package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class USAndStatesVaccinationsJanssenRequest {

    companion object {
        private const val URL = "https://data.cdc.gov/resource/w9zu-fywh.json"
    }

    fun getResult(): USAndStatesVaccinationsResult? {
        try {
            val dataJSON = URL(URL).readText()
            return Gson().fromJson(dataJSON, USAndStatesVaccinationsResult::class.java)
        } catch (exception: Exception) {
            println(exception)
        }
        return null
    }

}