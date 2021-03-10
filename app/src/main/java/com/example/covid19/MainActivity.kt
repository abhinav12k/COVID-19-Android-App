package com.example.covid19

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.Html
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
import com.example.covid19.data.AllStatesRequest
import com.example.covid19.data.SelectedStateRequest
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
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

        // DEFAULT DATA
        setDefaultData()

        // TODAY'S DATE
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, MMM d, yyyy").format(calendar.time)

        dateTextView.text = date

        // SPINNER
        // SPINNER STYLING AND ITEM DECLARATION
        val statesMap = setStates()
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
                this,
                R.array.states, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
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
                val selected = adapterView?.getItemAtPosition(position).toString().replace("\\s".toRegex(), "")
                // IF NOT DEFAULT STATE
                if (selected != "AllStates") {
                    doAsync {
                        uiThread {
                            // ALLOW RUN IN NETWORK ON MAIN THREAD
                            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                            StrictMode.setThreadPolicy(policy)
                            // GET API RESULT
                            val result = SelectedStateRequest().getResult(statesMap[selected]?.toLowerCase().toString())
                            if (result != null) {
                                val positiveText = if (result.getCurrPositives() != 0) NumberFormat.getNumberInstance(Locale.US).format(result.getCurrPositives()) else "Unknown"
                                val hospitalizedText = if (result.getCurrHospitalized() != 0) NumberFormat.getNumberInstance(Locale.US).format(result.getCurrHospitalized()) else "Unknown"
                                val deathsText = if (result.getCurrDeaths() != 0) NumberFormat.getNumberInstance(Locale.US).format(result.getCurrDeaths()) else "Unknown"

                                latestUpdateTitleTextView.text = "${statesMap[selected]} Latest Update"
                                latestUpdateTextView.text = "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(result.getLastDateModified())}"
                                positivesTextView.text = positiveText
                                hospitalizedTextView.text = hospitalizedText
                                deathsTextView.text = deathsText
                            }
                        }
                    }
                } else {
                    setDefaultData()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // IGNORE (THERE IS ALWAYS AN ELEMENT SELECTED)
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
        if (detailsLinkTextView != null) {
            // REMOVE UNDERLINE FROM HYPERTEXT
            val s = Html.fromHtml("<a href='https://covid.cdc.gov/covid-data-tracker/#datatracker-home'>Details</a>") as Spannable
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
        }

        // REMOVE TITLE BAR
        if (supportActionBar != null)
            supportActionBar?.hide()

        // SUPPRESS DARK MODE
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setDefaultData() {
        doAsync {
            val result = AllStatesRequest().getResult()
            uiThread {
                if (result != null) {
                    latestUpdateTextView.text = "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a").format(result.getLastDateModified())}"
                    positivesTextView.text = NumberFormat.getNumberInstance(Locale.US).format(result.getCurrPositives())
                    hospitalizedTextView.text =  NumberFormat.getNumberInstance(Locale.US).format(result.getCurrHospitalized())
                    deathsTextView.text = NumberFormat.getNumberInstance(Locale.US).format(result.getCurrDeaths())
                }
            }
        }
    }

    private fun setStates(): Map<String, String> {
        return mapOf(
                "Alaska" to "AK",
                "Alabama" to "AL",
                "Arkansas" to "AR",
                "Arizona" to "AZ",
                "California" to "CA",
                "Colorado" to "CO",
                "Connecticut" to "CT",
                "Delaware" to "DE",
                "Florida" to "FL",
                "Georgia" to "GA",
                "Hawaii" to "HI",
                "Iowa" to "IA",
                "Idaho" to "ID",
                "Illinois" to "IL",
                "Indiana" to "IN",
                "Kansas" to "KS",
                "Kentucky" to "KY",
                "Louisiana" to "LA",
                "Massachusetts" to "MA",
                "Maryland" to "MD",
                "Maine" to "ME",
                "Michigan" to "MI",
                "Minnesota" to "MN",
                "Missouri" to "MO",
                "Mississippi" to "MS",
                "Montana" to "MT",
                "NorthCarolina" to "NC",
                "NorthDakota" to "ND",
                "Nebraska" to "NE",
                "NewHampshire" to "NH",
                "NewJersey" to "NJ",
                "NewMexico" to "NM",
                "Nevada" to "NV",
                "NewYork" to "NY",
                "Ohio" to "OH",
                "Oklahoma" to "OK",
                "Oregon" to "OR",
                "Pennsylvania" to "PA",
                "RhodeIsland" to "RI",
                "SouthCarolina" to "SC",
                "SouthDakota" to "SD",
                "Tennessee" to "TN",
                "Texas" to "TX",
                "Utah" to "UT",
                "Virginia" to "VA",
                "Vermont" to "VT",
                "Washington" to "WA",
                "Wisconsin" to "WI",
                "WestVirginia" to "WV",
                "Wyoming" to "WY",
        )
    }
}