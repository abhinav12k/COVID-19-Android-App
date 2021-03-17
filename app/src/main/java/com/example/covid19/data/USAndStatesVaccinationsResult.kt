package com.example.covid19.data

import kotlin.collections.ArrayList

class USAndStatesVaccinationsResult : ArrayList<USAndStatesVaccinationsItem>() {

    fun getVaccines(state : String) : List<USAndStatesVaccinationsItem> {
        return this.filter { ((it.jurisdiction).replace("*", "")).replace(",", "") == state }
    }
    
}