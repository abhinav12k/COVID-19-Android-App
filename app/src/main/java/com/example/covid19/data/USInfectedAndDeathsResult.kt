package com.example.covid19.data

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class USInfectedAndDeathsResult : ArrayList<USInfectedAndDeathsItem>() {
    fun getInfected(): Int {
        return this[this.size - 1].Confirmed
    }

    fun getDeaths(): Int {
        return this[this.size - 1].Deaths
    }

    fun getDateUpdated(): Date {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(this[this.size - 1].Date)!!
    }

}
