package com.example.covid19.data

import com.google.gson.Gson
import java.net.URL

class SelectedStateRequest {

    companion object {
        private const val URL = "https://api.covidtracking.com/v1/states/"
    }

    fun getResult(state: String): SelectedStateRequestResult? {
        val data = URL("$URL$state/daily.json").readText()
        return Gson().fromJson(data, SelectedStateRequestResult::class.java)
    }
}