package com.example.covid19

import android.content.Intent
import android.content.res.Resources
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
    lateinit var questionnaireCardView: CardView
    lateinit var statesSpinner: Spinner
    lateinit var dateTextView: TextView
    lateinit var settingsImageView: ImageView
    lateinit var latestUpdateTitleTextView: TextView
    lateinit var latestUpdateTextView: TextView
    lateinit var positivesTextView: TextView
    lateinit var hospitalizedTextView: TextView
    lateinit var deathsTextView: TextView
    lateinit var detailsLinkTextView: TextView

    lateinit var usInfectedAndDeaths: USInfectedAndDeathsResult

    lateinit var usInfectedOverTime: MutableMap<String, Int>
    lateinit var usDeathsOverTime: MutableMap<String, Int>

    lateinit var statesInfectedAndDeaths: StatesInfectedAndDeathsResult

    lateinit var statesInfectedOverTime : MutableMap<String, List<StatesInfectedAndDeathsItem>>
    lateinit var statesDeathsOverTime : MutableMap<String, List<StatesInfectedAndDeathsItem>>

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

        // REQUEST ALL DATA AND SET DEFAULT DATA
        // ALLOW RUN IN NETWORK ON MAIN THREAD
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        doAsync {
            uiThread {
                try {
                    usInfectedAndDeaths = USInfectedAndDeathsRequest().getResult()!!

                    usInfectedOverTime = mutableMapOf()
                    usDeathsOverTime = mutableMapOf()

                    usInfectedAndDeaths.forEach { e ->
                        usInfectedOverTime[e.Date] = e.Confirmed
                        usDeathsOverTime[e.Date] = e.Deaths
                    }

                    statesInfectedAndDeaths = StatesInfectedAndDeathsRequest().getResult()!!

                    statesInfectedOverTime = mutableMapOf()
                    statesDeathsOverTime = mutableMapOf()

                    resources.getStringArray(R.array.states).forEach { e ->
                        statesInfectedOverTime[e] = statesInfectedAndDeaths.getInfected(e)
                        statesDeathsOverTime[e] = statesInfectedAndDeaths.getDeaths(e)
                    }

                    // SET DEFAULT DATA (U.S NUMBERS)
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

        // SPINNER
        // SPINNER AND SPINNER ITEM STYLING AND DECLARATION
        val statesMap = States().getStatesMap()
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
                // IF VALID STATE SELECTED
                if (selected != "All States") {
                    val infected = statesInfectedOverTime[selected]!!
                    val deaths = statesDeathsOverTime[selected]!!
                    val lastInfected = infected[infected.size - 1]
                    val lastDeaths = deaths[deaths.size - 1]
                    latestUpdateTitleTextView.text = " ${States().getStatesMap()[adapterView?.getItemAtPosition(position)]} Latest Updates"
                    latestUpdateTextView.text = "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(lastInfected.Date))}"
                    positivesTextView.text = NumberFormat.getNumberInstance(Locale.US).format(lastInfected.Confirmed)
                    hospitalizedTextView.text = "Unknown"
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
            latestUpdateTextView.text = "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(usInfectedAndDeaths.getDateUpdated())}"
            positivesTextView.text = NumberFormat.getNumberInstance(Locale.US).format(usInfectedAndDeaths.getInfected())
            hospitalizedTextView.text = "Unknown"
            deathsTextView.text = NumberFormat.getNumberInstance(Locale.US).format(usInfectedAndDeaths.getDeaths())
        } catch (exception: Exception) {
            println(exception)
        }
    }
}