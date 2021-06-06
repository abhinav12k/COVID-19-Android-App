package com.application.covid19.data

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class USInfectedAndDeathsResult : ArrayList<USInfectedAndDeathsItem>() {
    /**
     * Returns total U.S COVID-19 infected
     */
    fun getInfected(): Int {
        return this[this.size - 1].Confirmed
    }

    /**
     * Returns total U.S COVID-19 deaths
     */
    fun getDeaths(): Int {
        return this[this.size - 1].Deaths
    }

    /**
     * Returns last date the U.S COVID-19 data was updated
     */
    fun getDateUpdated(): Date {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(this[this.size - 1].Date)!!
    }
}
