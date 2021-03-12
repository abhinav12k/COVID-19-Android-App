package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class USAndStatesVaccinationsModernaRequest {

    companion object {
        private const val URL = "https://data.cdc.gov/resource/b7pe-5nws.json"
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