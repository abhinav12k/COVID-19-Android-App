package com.example.covid19.data

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AllStatesRequestResult : ArrayList<AllStatesRequestItem>() {
    fun getCurrPositives(): Int? {
        var lastKnown = 0
        while (this[lastKnown].positive == null) {
            lastKnown++
        }
        return this[lastKnown].positive
    }

    fun getCurrHospitalized(): Int? {
        var lastKnown = 0
        while (this[lastKnown].hospitalized == null) {
            lastKnown++
        }
        return this[lastKnown].hospitalized
    }

    fun getCurrDeaths(): Int? {
        var lastKnown = 0
        while (this[lastKnown].death == null) {
            lastKnown++
        }
        return this[lastKnown].death
    }
    fun getLastDateModified(): Date? {
        var lastKnown = 0
        while (this[lastKnown].dateChecked == null) {
            lastKnown++
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(this[lastKnown].dateChecked)
    }
}
