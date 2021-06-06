package com.application.covid19.data

import com.google.gson.Gson
import java.net.URL

class VaccinationsJanssenRequest {
    companion object {
        private const val URL = "https://data.cdc.gov/resource/w9zu-fywh.json"
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