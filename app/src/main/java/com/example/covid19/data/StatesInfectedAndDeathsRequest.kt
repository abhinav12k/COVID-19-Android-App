package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class StatesInfectedAndDeathsRequest {

    companion object {
        private const val URL = "https://api.covid19api.com/live/country/united-states"
    }

    fun getResult() : StatesInfectedAndDeathsResult? {
        try {
            val data = URL(URL).readText()
            return Gson().fromJson(data, StatesInfectedAndDeathsResult::class.java)
        } catch (exception: Exception) {
            println(exception)
        }
        return null
    }

}