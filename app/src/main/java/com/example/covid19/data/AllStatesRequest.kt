package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class AllStatesRequest {

    companion object {
        private const val URL = "https://api.covidtracking.com/v1/us/daily.json"
    }

    fun getResult(): AllStatesRequestResult? {
        val dataJSON = URL(URL).readText()
        return Gson().fromJson(dataJSON, AllStatesRequestResult::class.java)
    }
}