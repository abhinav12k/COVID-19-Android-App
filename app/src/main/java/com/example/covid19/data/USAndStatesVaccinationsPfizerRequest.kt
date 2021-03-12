package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class USAndStatesVaccinationsPfizerRequest {

    companion object {
        private const val URL = "https://data.cdc.gov/resource/saz5-9hgg.json"
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