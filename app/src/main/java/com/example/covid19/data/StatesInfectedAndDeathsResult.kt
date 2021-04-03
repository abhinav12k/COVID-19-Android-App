package com.example.covid19.data

import kotlin.collections.ArrayList

class StatesInfectedAndDeathsResult : ArrayList<StatesInfectedAndDeathsItem>() {
    /**
     * Returns a List of [StatesInfectedAndDeathsItem] objects from the [state] passed sorted by date
     */
    fun getInfectedAndDeaths(state : String): List<StatesInfectedAndDeathsItem> {
        return this.filter { it.Province == state }.sortedWith(compareBy{ it.Date })
    }
}
