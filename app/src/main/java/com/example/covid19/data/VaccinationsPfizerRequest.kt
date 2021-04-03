package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class VaccinationsPfizerRequest {
    companion object {
        private const val URL = "https://data.cdc.gov/resource/saz5-9hgg.json"
    }

    /**
     * Fetches [URL] and returns a [VaccinationsResult]
     */
    fun getResult(): VaccinationsResult? {
        try {
            val dataJSON = URL(URL).readText()
            return Gson().fromJson(dataJSON, VaccinationsResult::class.java)
        } catch (exception: Exception) {
            println(exception)
        }
        return null
    }
}