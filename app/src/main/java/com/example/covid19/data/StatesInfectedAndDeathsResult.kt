package com.example.covid19.data

import kotlin.collections.ArrayList

class StatesInfectedAndDeathsResult : ArrayList<StatesInfectedAndDeathsItem>() {
    fun getInfected(state : String): List<StatesInfectedAndDeathsItem> {
        return this.filter { it.Province == state }.sortedWith(compareBy{ it.Date })
    }

    fun getDeaths(state : String): List<StatesInfectedAndDeathsItem> {
        return this.filter { it.Province == state }.sortedWith(compareBy { it.Date })
    }
}
