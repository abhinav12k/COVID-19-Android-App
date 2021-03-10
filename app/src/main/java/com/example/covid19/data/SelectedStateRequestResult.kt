package com.example.covid19.data

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SelectedStateRequestResult : ArrayList<SelectedStateRequestItem>() {
    fun getCurrPositives(): Int? {
        var lastKnown = 0
        while (this[lastKnown].positive == null) {
            lastKnown++
            if (lastKnown == this.size){
                return 0
            }
        }
        return this[lastKnown].positive
    }

    fun getCurrHospitalized(): Int? {
        var lastKnown = 0
        while (this[lastKnown].hospitalized == null) {
            lastKnown++
            if (lastKnown == this.size){
                return 0
            }
        }
        return this[lastKnown].hospitalized
    }

    fun getCurrDeaths(): Int? {
        var lastKnown = 0
        while (this[lastKnown].death == null) {
            lastKnown++
            if (lastKnown == this.size){
                return 0
            }
        }
        return this[lastKnown].death
    }

    fun getLastDateModified(): Date? {
        var lastKnown = 0
        while (this[lastKnown].dateModified == null) {
            lastKnown++
            if (lastKnown == this.size){
                return Calendar.getInstance().run { time }
            }
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(this[lastKnown].dateModified)
    }
}
