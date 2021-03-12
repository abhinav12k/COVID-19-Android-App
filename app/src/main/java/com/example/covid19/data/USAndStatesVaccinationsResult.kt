package com.example.covid19.data

import kotlin.collections.ArrayList

class USAndStatesVaccinationsResult : ArrayList<USAndStatesVaccinationsItem>() {

    fun getVaccines(state : String) : List<USAndStatesVaccinationsItem> {
        return this.filter { it.jurisdiction == state }.sortedWith(compareBy{ it.week_of_allocations })
    }
    
}