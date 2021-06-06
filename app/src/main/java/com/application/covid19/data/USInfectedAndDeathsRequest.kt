package com.application.covid19.data

import com.google.gson.Gson
import java.net.URL

class USInfectedAndDeathsRequest {
    companion object {
        private const val URL = "https://api.covid19api.com/total/country/united-states"
    }

    /**
     * Fetches [URL] and returns a [USInfectedAndDeathsResult]
     */
    fun getResult(): USInfectedAndDeathsResult? {
        try {
            val dataJSON = URL(URL).readText()
            return Gson().fromJson(dataJSON, USInfectedAndDeathsResult::class.java)
        } catch (exception: Exception) {
            println(exception)
        }
        return null
    }
}