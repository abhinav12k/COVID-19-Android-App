package com.example.covid19

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.covid19.data.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ALLOW RUN IN NETWORK ON MAIN THREAD
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // GET STRING ARRAY FROM STRINGS.XML
        val states = resources.getStringArray(R.array.states);

        // REQUEST AND FILTER DATA
        doAsync {
            uiThread {
                // REQUEST FROM https://data.cdc.gov/
                try {
                    // REQUEST U.S AND STATES VACCINES DATA
                    val pfizerUSAndStatesVaccinations = USAndStatesVaccinationsPfizerRequest().getResult()!!
                    val modernaUSAndStatesVaccinations = USAndStatesVaccinationsModernaRequest().getResult()!!
                    val janssenUSAndStatesVaccinations = USAndStatesVaccinationsJanssenRequest().getResult()!!

                    // COMBINE DATA
                    val usCombinedVaccinations = (pfizerUSAndStatesVaccinations + modernaUSAndStatesVaccinations + janssenUSAndStatesVaccinations)
                            .sortedWith(compareBy { it.week_of_allocations })

                    // INITIALIZE EMPTY MAP OF <STRING, LIST<ITEM>>
                    val statesVaccinatedOverTime = mutableMapOf<String, List<USAndStatesVaccinationsItem>>()

                    // POPULATE STATES VACCINES MAP
                    states.forEach { state ->
                        if (state != "All States") {
                            val combinedStatesVaccinations = (pfizerUSAndStatesVaccinations.getVaccines(state) + modernaUSAndStatesVaccinations.getVaccines(state) + janssenUSAndStatesVaccinations.getVaccines(state))
                                .sortedWith(compareBy { it.week_of_allocations })
                            statesVaccinatedOverTime[state] = combinedStatesVaccinations
                        }
                    }

                    // ADD DATA TO SHARED PREFERENCES
                    addDATACDCToSharedPreferences(usCombinedVaccinations, statesVaccinatedOverTime)
                } catch (e: Exception) {
                    println(e)
                    Toast.makeText(this@SplashActivity, "CDC API Failed to Respond", Toast.LENGTH_SHORT).show()
                }

                // REQUEST FROM https://api.covid19api.com/
                try {
                    // REQUEST U.S INFECTED AND DEATHS DATA
                    val usInfectedAndDeaths = USInfectedAndDeathsRequest().getResult()!!

                    // REQUEST STATES INFECTED AND DEATHS DATA
                    val statesInfectedAndDeaths = StatesInfectedAndDeathsRequest().getResult()!!

                    // INITIALIZE EMPTY MAPS OF <STRING, LIST<ITEM>>
                    var statesInfectedOverTime = mutableMapOf<String, List<StatesInfectedAndDeathsItem>>()
                    var statesDeathsOverTime = mutableMapOf<String, List<StatesInfectedAndDeathsItem>>()

                    states.forEach { e ->
                        statesInfectedOverTime[e] = statesInfectedAndDeaths!!.getInfected(e)
                        statesDeathsOverTime[e] = statesInfectedAndDeaths!!.getDeaths(e)
                    }

                    // ADD DATA TO SHARED PREFERENCES
                    addCOVID19APIToSharedPreferences(usInfectedAndDeaths, statesInfectedOverTime, statesDeathsOverTime)
                } catch (e: Exception) {
                    println(e)
                    Toast.makeText(this@SplashActivity, "COVID19 API Failed to Respond", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // REMOVE TITLE BAR
        if (supportActionBar != null)
            supportActionBar?.hide()

        // SUPPRESS DARK MODE
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // START MAIN ACTIVITY ONCE ALL DATA HAS BEEN SAVED TO SHARED PREFERENCES
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun addDATACDCToSharedPreferences(
        usCombinedVaccinations: List<USAndStatesVaccinationsItem>,
        statesVaccinatedOverTime: MutableMap<String, List<USAndStatesVaccinationsItem>>
    ) {
        // ACCESS TO SHARED PREFERENCES AND EDITOR
        val sharedPreferences: SharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE);
        val editor = sharedPreferences.edit();

        // GET STRING ARRAY FROM STRINGS.XML
        val states = resources.getStringArray(R.array.states);

        // ADD STATES VACCINATION TOTALS TO SHARED PREFERENCES
        addDATACDCToSharedPreferencesSTATES(editor, states, statesVaccinatedOverTime)

        // ADD U.S VACCINATION TOTALS TO SHARED PREFERENCES
        addDATACDCToSharedPreferencesUS(editor, usCombinedVaccinations)

        // SAVE CHANGES
        editor.commit()
    }

    private fun addDATACDCToSharedPreferencesUS(
        editor: SharedPreferences.Editor,
        usCombinedVaccinations: List<USAndStatesVaccinationsItem>
    ) {
        // GET LISTS SIZE
        val usVaccinatedListSize = usCombinedVaccinations.size

        // REDUCE U.S VACCINATIONS TO A MAP OF <DATE, VACCINATIONS>
        val usReducedVaccinations = mutableMapOf<String, Int>()

        for (i in 0 until usVaccinatedListSize) {
            if (usCombinedVaccinations[i].week_of_allocations in usReducedVaccinations) {
                usReducedVaccinations[usCombinedVaccinations[i].week_of_allocations] = usReducedVaccinations[usCombinedVaccinations[i].week_of_allocations]!! + usCombinedVaccinations[i]._1st_dose_allocations.toInt()
            } else {
                usReducedVaccinations[usCombinedVaccinations[i].week_of_allocations] = usCombinedVaccinations[i]._1st_dose_allocations.toInt()
            }
        }

        // VARIABLES FOR WEEKLY AND CUMULATIVE VALUES
        var usVaccinatedTotal = 0
        var usVaccinatedDifference = 0
        var usVaccinatedIndex = 0

        // ADD REDUCED U.S VACCINATION TO SHARED PREFERENCES
        usReducedVaccinations.forEach { (date, vaccinations) ->
            // CALCULATE TOTALS AND DIFFERENCE
            usVaccinatedDifference = vaccinations
            usVaccinatedTotal += vaccinations

            editor.putString("US_VACCINATED_LIST_${usVaccinatedIndex}", vaccinations.toString())
            editor.putString("US_VACCINATED_LIST_SUM_${usVaccinatedIndex}", usVaccinatedTotal.toString())
            editor.putString("US_VACCINATED_LIST_DATE_${usVaccinatedIndex}", date)

            // INCREMENT INDEX
            usVaccinatedIndex++
        }

        // ADD U.S VACCINATED LIST SIZE TO SHARED PREFERENCES
        editor.putInt("US_VACCINATED_LIST_SIZE", usVaccinatedIndex)

        // ADD U.S VACCINATION TOTAL TO SHARED PREFERENCES
        editor.putString("US_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(usVaccinatedTotal).toString())
        editor.putString("US_NEW_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(usVaccinatedDifference).toString())
    }

    private fun addDATACDCToSharedPreferencesSTATES(
        editor: SharedPreferences.Editor,
        states: Array<String>,
        statesVaccinatedOverTime: MutableMap<String, List<USAndStatesVaccinationsItem>>
    ) {
        states.forEach { state ->
            if (state != "All States") {
                // FORMAT STATE STRING TO MATCH NAMING CONVENTION
                val stateFormatted = state.replace("\\s".toRegex(), "").toUpperCase()

                // GET STATE INFECTED AND DEATHS LIST FROM MUTABLE MAP
                val stateCombinedVaccinations = statesVaccinatedOverTime[state]!!

                // GET LISTS SIZE
                val stateVaccinatedListSize = stateCombinedVaccinations.size

                // REDUCE STATE VACCINATIONS TO A MAP OF <DATE, VACCINATIONS>
                val statesReducedVaccinations = mutableMapOf<String, Int>()

                for (i in 0 until stateVaccinatedListSize) {
                    if (stateCombinedVaccinations[i].week_of_allocations in statesReducedVaccinations) {
                        statesReducedVaccinations[stateCombinedVaccinations[i].week_of_allocations] = statesReducedVaccinations[stateCombinedVaccinations[i].week_of_allocations]!! + stateCombinedVaccinations[i]._1st_dose_allocations.toInt()
                    } else {
                        statesReducedVaccinations[stateCombinedVaccinations[i].week_of_allocations] = stateCombinedVaccinations[i]._1st_dose_allocations.toInt()
                    }
                }

                // VARIABLES FOR WEEKLY AND CUMULATIVE VALUES
                var stateVaccinatedTotal = 0
                var stateVaccinatedNew = 0
                var stateVaccinatedIndex = 0

                // ADD REDUCED U.S VACCINATION TO SHARED PREFERENCES
                statesReducedVaccinations.forEach { (date, vaccinations) ->
                    // CALCULATE TOTALS AND DIFFERENCE
                    stateVaccinatedNew = vaccinations
                    stateVaccinatedTotal += vaccinations

                    editor.putString("${stateFormatted}_VACCINATED_LIST_${stateVaccinatedIndex}", vaccinations.toString())
                    editor.putString("${stateFormatted}_VACCINATED_LIST_SUM_${stateVaccinatedIndex}", stateVaccinatedTotal.toString())
                    editor.putString("${stateFormatted}_VACCINATED_LIST_DATE_${stateVaccinatedIndex}", date)

                    // INCREMENT INDEX
                    stateVaccinatedIndex++
                }

                // ADD STATE VACCINATED LIST SIZE TO SHARED PREFERENCES
                editor.putInt("${stateFormatted}_VACCINATED_LIST_SIZE", stateVaccinatedIndex)

                // ADD U.S VACCINATION TOTAL TO SHARED PREFERENCES
                editor.putString("${stateFormatted}_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(stateVaccinatedTotal).toString())
                editor.putString("${stateFormatted}_NEW_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(stateVaccinatedNew).toString())
            }
        }
    }

    private fun addCOVID19APIToSharedPreferences(
        usInfectedAndDeaths: USInfectedAndDeathsResult,
        statesInfectedOverTime: MutableMap<String, List<StatesInfectedAndDeathsItem>>,
        statesDeathsOverTime: MutableMap<String, List<StatesInfectedAndDeathsItem>>
    ) {
        // ACCESS TO SHARED PREFERENCES AND EDITOR
        val sharedPreferences: SharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE);
        val editor = sharedPreferences.edit();

        // GET STRING ARRAY FROM STRINGS.XML
        val states = resources.getStringArray(R.array.states);

        // ADD STATES VACCINATION TOTALS TO SHARED PREFERENCES
        addCOVID19APIToSharedPreferencesSTATES(editor, states, statesInfectedOverTime, statesDeathsOverTime)

        // ADD U.S VACCINATION TOTALS TO SHARED PREFERENCES
        addCOVID19APIToSharedPreferencesUS(editor, usInfectedAndDeaths)

        // SAVE CHANGES
        editor.commit()
    }

    private fun addCOVID19APIToSharedPreferencesUS(
        editor: SharedPreferences.Editor,
        usInfectedAndDeaths: USInfectedAndDeathsResult
    ) {
        // GET U.S INFECTED AND DEATHS LIST FROM REQUEST RESULT
        val infectedAndDeaths = usInfectedAndDeaths!!

        // GET LIST SIZE
        val usListSize = infectedAndDeaths.size

        // ADD INFECTED AND DEATH LIST SIZE TO SHARED PREFERENCES
        editor.putInt("US_INFECTED_LIST_SIZE", usListSize)
        editor.putInt("US_DEATHS_LIST_SIZE", usListSize)

        // VARIABLES FOR DAILY VALUES
        var infectedDifference = 0
        var deathDifference = 0

        // VARIABLES FOR CUMULATIVE VALUES
        var infectedTotal = 0
        var deathTotal = 0

        for (i in 0 until usListSize) {
            // CALCULATE DAILY VALUES
            infectedDifference = (infectedAndDeaths[i].Confirmed - infectedTotal)
            deathDifference = (infectedAndDeaths[i].Deaths - deathTotal)

            // GET CUMULATIVE VALUES
            infectedTotal = infectedAndDeaths[i].Confirmed
            deathTotal = infectedAndDeaths[i].Deaths

            // ADD DAILY VALUES TO SHARED PREFERENCES
            editor.putString("US_INFECTED_LIST_$i", (infectedDifference).toString())
            editor.putString("US_DEATHS_LIST_$i", (deathDifference).toString())

            // ADD CUMULATIVE VALUES TO SHARED PREFERENCES
            editor.putString("US_INFECTED_LIST_SUM_$i", infectedTotal.toString())
            editor.putString("US_DEATHS_LIST_SUM_$i", deathTotal.toString())

            // ADD DATES TO SHARED PREFERENCES
            editor.putString("US_INFECTED_LIST_DATE_$i", infectedAndDeaths[i].Date)
            editor.putString("US_DEATHS_LIST_DATE_$i", infectedAndDeaths[i].Date)
        }

        // ADD U.S TOTALS
        editor.putString("US_UPDATED", "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(infectedAndDeaths.getDateUpdated())}")
        editor.putString("US_NEW_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(infectedDifference))
        editor.putString("US_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(infectedAndDeaths.getInfected()))
        editor.putString("US_NEW_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(deathDifference))
        editor.putString("US_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(infectedAndDeaths.getDeaths()))
    }

    private fun addCOVID19APIToSharedPreferencesSTATES(
        editor: SharedPreferences.Editor,
        states: Array<String>,
        statesInfectedOverTime: MutableMap<String, List<StatesInfectedAndDeathsItem>>,
        statesDeathsOverTime: MutableMap<String, List<StatesInfectedAndDeathsItem>>
    ) {
        states.forEach { state ->
            if (state != "All States") {
                // FORMAT STATE STRING TO MATCH NAMING CONVENTION
                val stateFormatted = state.replace("\\s".toRegex(), "").toUpperCase()

                // GET STATE INFECTED AND DEATHS LIST FROM MUTABLE MAP
                val infected = statesInfectedOverTime[state]!!
                val deaths = statesDeathsOverTime[state]!!

                // GET LISTS SIZE
                val stateInfectedListSize = infected.size
                val stateDeathsListSize = deaths.size

                // GET LAST INFECTED AND DEATHS ITEM IN LIST
                val lastInfected = infected[infected.size - 1]
                val lastDeaths = deaths[deaths.size - 1]

                // ADD STATE INFECTED VALUES TO SHARED PREFERENCES
                // ADD INFECTED LIST SIZE TO SHARED PREFERENCES
                editor.putInt("${stateFormatted}_INFECTED_LIST_SIZE", stateInfectedListSize)

                // VARIABLES FOR DAILY VALUES
                var stateInfectedDifference = 0
                var stateInfectedTotal = 0

                for (i in 0 until stateInfectedListSize) {
                    // CALCULATE DAILY AND CUMULATIVE VALUES
                    stateInfectedDifference = (infected[i].Confirmed - stateInfectedTotal)
                    stateInfectedTotal = infected[i].Confirmed

                    // ADD DAILY VALUES, CUMULATIVE VALUES, AND DATES TO SHARED PREFERENCES
                    editor.putString("${stateFormatted}_INFECTED_LIST_$i", stateInfectedDifference.toString())
                    editor.putString("${stateFormatted}_INFECTED_LIST_SUM_$i", stateInfectedTotal.toString())
                    editor.putString("${stateFormatted}_INFECTED_LIST_DATE_$i", infected[i].Date)
                }

                // ADD STATE DEATH VALUES TO SHARED PREFERENCES
                // ADD DEATH LIST SIZE TO SHARED PREFERENCES
                editor.putInt("${stateFormatted}_DEATHS_LIST_SIZE", stateDeathsListSize)

                // VARIABLES FOR DEATHS DAILY VALUES
                var stateDeathsDifference = 0
                var stateDeathsTotal = 0

                for (i in 0 until stateDeathsListSize) {
                    // CALCULATE DAILY AND CUMULATIVE VALUES
                    stateDeathsDifference = (deaths[i].Deaths - stateDeathsTotal)
                    stateDeathsTotal = deaths[i].Deaths

                    // ADD DAILY VALUES, CUMULATIVE VALUES, AND DATES TO SHARED PREFERENCES
                    editor.putString("${stateFormatted}_DEATHS_LIST_$i", stateDeathsDifference.toString())
                    editor.putString("${stateFormatted}_DEATHS_LIST_SUM_$i", stateDeathsTotal.toString())
                    editor.putString("${stateFormatted}_DEATHS_LIST_DATE_$i", deaths[i].Date)
                }

                // ADD STATE TOTALS
                editor.putString("${stateFormatted}_UPDATED", "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(lastInfected.Date))}")
                editor.putString("${stateFormatted}_NEW_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(stateInfectedDifference))
                editor.putString("${stateFormatted}_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(lastInfected.Confirmed))
                editor.putString("${stateFormatted}_NEW_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(stateDeathsDifference))
                editor.putString("${stateFormatted}_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(lastDeaths.Deaths))
            }
        }
    }
}