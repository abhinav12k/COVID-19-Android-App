package com.example.covid19

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import com.example.covid19.data.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    // INITIALIZE VARIABLES
    private lateinit var questionnaireCardView: CardView
    private lateinit var statesSpinner: Spinner
    private lateinit var dateTextView: TextView
    private lateinit var settingsImageView: ImageView
    private lateinit var latestUpdateTitleTextView: TextView
    private lateinit var latestUpdateTextView: TextView
    private lateinit var positivesTextView: TextView
    private lateinit var hospitalizedTextView: TextView
    private lateinit var deathsTextView: TextView
    private lateinit var detailsLinkTextView: TextView

    private lateinit var usInfectedAndDeaths: USInfectedAndDeathsResult
    private lateinit var usInfectedOverTime: MutableMap<String, Int>
    private lateinit var usDeathsOverTime: MutableMap<String, Int>

    private lateinit var statesInfectedAndDeaths: StatesInfectedAndDeathsResult
    private lateinit var statesInfectedOverTime : MutableMap<String, List<StatesInfectedAndDeathsItem>>
    private lateinit var statesDeathsOverTime : MutableMap<String, List<StatesInfectedAndDeathsItem>>

    private lateinit var pfizerUSAndStatesVaccinations: USAndStatesVaccinationsResult
    private lateinit var modernaUSAndStatesVaccinations: USAndStatesVaccinationsResult
    private lateinit var janssenUSAndStatesVaccinations: USAndStatesVaccinationsResult

    private lateinit var usVaccinatedOverTime: MutableMap<String, Int>
    private lateinit var statesVaccinatedOverTime: MutableMap<String, List<USAndStatesVaccinationsItem>>
    private var usVaccinations: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        // SHOW LAYOUT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GET COMPONENTS
        questionnaireCardView = findViewById(R.id.cv_questionnaire_button)
        statesSpinner = findViewById(R.id.sp_states)
        dateTextView = findViewById(R.id.tv_date)
        settingsImageView = findViewById(R.id.iv_settings)
        latestUpdateTitleTextView = findViewById(R.id.tv_latest_update_title)
        latestUpdateTextView = findViewById(R.id.tv_latest_update)
        detailsLinkTextView = findViewById(R.id.tv_details)
        positivesTextView = findViewById(R.id.tv_positive)
        hospitalizedTextView = findViewById(R.id.tv_hospitalized)
        deathsTextView = findViewById(R.id.tv_deaths)

        // ALLOW RUN IN NETWORK ON MAIN THREAD
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        // REQUEST DATA
        doAsync {
            uiThread {
                try {
                    // INITIALIZE MAPS
                    usInfectedOverTime = mutableMapOf()
                    usDeathsOverTime = mutableMapOf()
                    usVaccinatedOverTime = mutableMapOf()
                    statesVaccinatedOverTime = mutableMapOf()
                    statesInfectedOverTime = mutableMapOf()
                    statesDeathsOverTime = mutableMapOf()

                    // REQUEST U.S INFECTED AND DEATHS DATA
                    usInfectedAndDeaths = USInfectedAndDeathsRequest().getResult()!!

                    // POPULATE U.S INFECTED AND DEATHS MAPS
                    usInfectedAndDeaths.forEach { e ->
                        usInfectedOverTime[e.Date] = e.Confirmed
                        usDeathsOverTime[e.Date] = e.Deaths
                    }

                    // REQUEST STATES INFECTED AND DEATHS DATA
                    statesInfectedAndDeaths = StatesInfectedAndDeathsRequest().getResult()!!

                    // POPULATE STATES INFECTED AND DEATHS  MAPS
                    resources.getStringArray(R.array.states).forEach { e ->
                        statesInfectedOverTime[e] = statesInfectedAndDeaths.getInfected(e)
                        statesDeathsOverTime[e] = statesInfectedAndDeaths.getDeaths(e)
                    }

                    // REQUEST U.S AND STATES VACCINES DATA
                    pfizerUSAndStatesVaccinations = USAndStatesVaccinationsPfizerRequest().getResult()!!
                    modernaUSAndStatesVaccinations = USAndStatesVaccinationsModernaRequest().getResult()!!
                    janssenUSAndStatesVaccinations = USAndStatesVaccinationsJanssenRequest().getResult()!!

                    // COMBINE DATA
                    val allUSAndStatesVaccinations = (pfizerUSAndStatesVaccinations + modernaUSAndStatesVaccinations + janssenUSAndStatesVaccinations)
                            .sortedWith(compareBy{ it.week_of_allocations })

                    // POPULATE STATES VACCINES MAP
                    resources.getStringArray(R.array.states).forEach { e ->
                        statesVaccinatedOverTime[e] = pfizerUSAndStatesVaccinations.getVaccines(e) + modernaUSAndStatesVaccinations.getVaccines(e) + janssenUSAndStatesVaccinations.getVaccines(e)
                                .sortedWith(compareBy{ it.week_of_allocations })
                    }

                    // POPULATE U.S VACCINES MAP
                    var total = 0
                    for (i in allUSAndStatesVaccinations.indices) {
                        total += allUSAndStatesVaccinations[i]._1st_dose_allocations.toInt()
                        usVaccinatedOverTime[allUSAndStatesVaccinations[i].week_of_allocations] = total
                    }

                    // GET TOTAL U.S VACCINATIONS
                    usVaccinations = total

                    // SET U.S DATA AS DEFAULT
                    setDefaultData()
                } catch (exception : Exception) {
                    println(exception)
                }
            }
        }

        // TODAY'S DATE
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, MMM d, yyyy").format(calendar.time)

        dateTextView.text = date

        // SPINNER AND SPINNER ITEM STYLING AND DECLARATION
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
                this,
                R.array.states, R.layout.spinner_states_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_states_dropdown_item)
        statesSpinner.adapter = adapter

        // SPINNER LISTENER
        statesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                // GET SELECTED STATE
                val selected = adapterView?.getItemAtPosition(position)

                if (selected != "All States") {
                    // GET ARRAY LISTS
                    val infected = statesInfectedOverTime[selected]!!
                    val deaths = statesDeathsOverTime[selected]!!
                    val vaccinated = statesVaccinatedOverTime[selected]!!
                    // GET LAST OBJECT
                    val lastInfected = infected[infected.size - 1]
                    val lastDeaths = deaths[deaths.size - 1]
                    // SUM ALL OBJECTS IN ARRAY LIST
                    var sumOfVaccinations = 0
                    vaccinated.forEach { e ->
                        sumOfVaccinations += e._1st_dose_allocations.toInt()
                    }
                    // ASSIGN DATA
                    latestUpdateTitleTextView.text = "${States().getStatesMap()[adapterView?.getItemAtPosition(position)]} Latest Updates"
                    latestUpdateTextView.text = "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(lastInfected.Date))}"
                    positivesTextView.text = NumberFormat.getNumberInstance(Locale.US).format(lastInfected.Confirmed)
                    hospitalizedTextView.text = NumberFormat.getNumberInstance(Locale.US).format(sumOfVaccinations)
                    deathsTextView.text = NumberFormat.getNumberInstance(Locale.US).format(lastDeaths.Deaths)
                } else {
                    setDefaultData()
                    latestUpdateTitleTextView.text = "US Latest Updates"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // IGNORE: THERE IS ALWAYS AN ELEMENT SELECTED
            }
        }

        // SETTINGS LINK
        settingsImageView.setOnClickListener(View.OnClickListener {
            // START SETTINGS ACTIVITY
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            // SKIP ACTIVITY TRANSITION ANIMATION
            this.overridePendingTransition(0, 0);
        })

        // QUESTIONNAIRE LINK
        questionnaireCardView.setOnClickListener(View.OnClickListener {
            // START QUESTIONNAIRE ACTIVITY
            val intent = Intent(this, QuestionnaireActivity::class.java)
            startActivity(intent)
            // SKIP ACTIVITY TRANSITION ANIMATION
            this.overridePendingTransition(0, 0);
        })

            // DETAILS LINK
            // REMOVE UNDERLINE FROM HYPERTEXT
            val s = HtmlCompat.fromHtml("<a href='https://covid.cdc.gov/covid-data-tracker/#datatracker-home'>Details</a>", HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
            for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
                s.setSpan(object : UnderlineSpan() {
                    override fun updateDrawState(tp: TextPaint) {
                        tp.isUnderlineText = false
                    }
                }, s.getSpanStart(u), s.getSpanEnd(u), 0)
            }
            detailsLinkTextView.text = s
             // OPEN WEB WHEN LINK CLICKED
            detailsLinkTextView.movementMethod = LinkMovementMethod.getInstance()

        // REMOVE TITLE BAR
        if (supportActionBar != null)
            supportActionBar?.hide()

        // SUPPRESS DARK MODE
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setDefaultData() {
        try {
            // ASSIGN DATA
            latestUpdateTextView.text = "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(usInfectedAndDeaths.getDateUpdated())}"
            positivesTextView.text = if (usInfectedAndDeaths.getInfected() != 0) NumberFormat.getNumberInstance(Locale.US).format(usInfectedAndDeaths.getInfected()) else "Unknown"
            hospitalizedTextView.text = if (usVaccinations != 0) NumberFormat.getNumberInstance(Locale.US).format(usVaccinations) else "Unknown"
            deathsTextView.text = if (usInfectedAndDeaths.getDeaths() != 0) NumberFormat.getNumberInstance(Locale.US).format(usInfectedAndDeaths.getDeaths()) else "Unknown"
        } catch (exception: Exception) {
            println(exception)
        }
    }
}