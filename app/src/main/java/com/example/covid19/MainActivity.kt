package com.example.covid19

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.example.covid19.data.*
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.model.Dataset
import com.yabu.livechart.view.LiveChart
import com.yabu.livechart.view.LiveChartStyle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    // INITIALIZE VARIABLES
    private lateinit var questionnaireCardView: CardView

    private lateinit var statesSpinner : Spinner
    private lateinit var vistypeSpinner : Spinner

    private lateinit var dateTextView : TextView
    private lateinit var settingsImageView : ImageView

    private lateinit var latestUpdateTitleTextView : TextView
    private lateinit var latestUpdateTextView : TextView

    private lateinit var infectedTextView : TextView
    private lateinit var vaccinatedTextView : TextView
    private lateinit var deathsTextView : TextView

    private lateinit var newInfectedTextView : TextView
    private lateinit var newVaccinatedTextView : TextView
    private lateinit var newDeathsTextView : TextView

    private lateinit var detailsLinkTextView : TextView

    private lateinit var lineChart : LiveChart

    private lateinit var lineChartDateTextView: TextView
    private lateinit var lineChartDailyTextView: TextView
    private lateinit var lineChartSumTextView: TextView

    private var stateSelected = ""
    private var visSelected = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // SHOW LAYOUT
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_white)
        setContentView(R.layout.activity_main)

        // GET COMPONENTS
        questionnaireCardView = findViewById(R.id.cv_questionnaire_button)

        statesSpinner = findViewById(R.id.sp_states)
        vistypeSpinner = findViewById(R.id.sp_vistype)

        dateTextView = findViewById(R.id.tv_date)
        settingsImageView = findViewById(R.id.iv_settings)

        latestUpdateTitleTextView = findViewById(R.id.tv_latest_update_title)
        latestUpdateTextView = findViewById(R.id.tv_latest_update)

        infectedTextView = findViewById(R.id.tv_infected)
        vaccinatedTextView = findViewById(R.id.tv_vaccinated)
        deathsTextView = findViewById(R.id.tv_deaths)

        newInfectedTextView = findViewById(R.id.tv_new_infected)
        newVaccinatedTextView = findViewById(R.id.tv_new_vaccinated)
        newDeathsTextView = findViewById(R.id.tv_new_deaths)

        detailsLinkTextView = findViewById(R.id.tv_details)

        lineChart = findViewById(R.id.line_chart)
        lineChartDateTextView = findViewById(R.id.live_chart_date)
        lineChartDailyTextView = findViewById(R.id.live_chart_daily)
        lineChartSumTextView = findViewById(R.id.live_chart_sum)

        // ACCESS SHARED PREFERENCES
        val sharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE);

        // TODAY'S DATE
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, MMM d, yyyy").format(calendar.time)
        dateTextView.text = date

        // SPINNER AND SPINNER ITEM STYLING AND DECLARATION
        // STATES SPINNER
        val statesSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
                this,
                R.array.states, R.layout.spinner_states_item
        )
        statesSpinnerAdapter.setDropDownViewResource(R.layout.spinner_states_dropdown_item)
        statesSpinner.adapter = statesSpinnerAdapter

        // VISUALIZATION TYPE SPINNER
        val vistypeSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
                this,
                R.array.vistype, R.layout.spinner_vistype_item
        )
        vistypeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_vistype_dropdown_item)
        vistypeSpinner.adapter = vistypeSpinnerAdapter

        // STATE SPINNER LISTENER
        statesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                // GET SELECTED STATE
                val selected = adapterView?.getItemAtPosition(position).toString().toUpperCase()

                // SET GLOBAL STATE SELECTED
                stateSelected = selected

                setLineChart(lineChart, sharedPreferences, visSelected, stateSelected)

                if (selected != "ALL STATES") {
                    val selectedFormatted = selected.replace("\\s".toRegex(), "")
                    latestUpdateTitleTextView.text = "${States().getStatesMap()[adapterView?.getItemAtPosition(position).toString()]} Cases Overview"

                    latestUpdateTextView.text = sharedPreferences.getString("${selectedFormatted}_UPDATED", "Unknown")

                    infectedTextView.text = sharedPreferences.getString("${selectedFormatted}_INFECTED", "Unknown")
                    vaccinatedTextView.text = sharedPreferences.getString("${selectedFormatted}_VACCINATED", "Unknown")
                    deathsTextView.text = sharedPreferences.getString("${selectedFormatted}_DEATHS", "Unknown")

                    val newInfected = sharedPreferences.getString("${selectedFormatted}_NEW_INFECTED", "0")
                    val newVaccinated = sharedPreferences.getString("${selectedFormatted}_NEW_VACCINATED", "0")
                    val newDeaths = sharedPreferences.getString("${selectedFormatted}_NEW_DEATHS", "0")

                    val formattedNewInfected = if (newInfected!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newInfected"
                    val formattedNewVaccinated = if (newVaccinated!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newVaccinated"
                    val formattedNewDeaths = if (newDeaths!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newDeaths"

                    newInfectedTextView.text = formattedNewInfected
                    newVaccinatedTextView.text = formattedNewVaccinated
                    newDeathsTextView.text = formattedNewDeaths
                } else {
                    latestUpdateTitleTextView.text = "US Cases Overview"
                    setDefaultData(sharedPreferences)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // IGNORE: THERE IS ALWAYS AN ELEMENT SELECTED
            }
        }

        // VISUALIZATION TYPE SPINNER LISTENER
        vistypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                // GET SELECTED VISUALIZATION TYPE
                val selected = adapterView?.getItemAtPosition(position).toString()

                visSelected = selected

                setLineChart(lineChart, sharedPreferences, visSelected, stateSelected)
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
        // OPEN LINK
        detailsLinkTextView.movementMethod = LinkMovementMethod.getInstance()

        // REMOVE TITLE BAR
        if (supportActionBar != null)
            supportActionBar?.hide()

        // SUPPRESS DARK MODE
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setDefaultData(sharedPreferences : SharedPreferences) {
        latestUpdateTextView.text = sharedPreferences.getString("US_UPDATED", "Unknown")

        infectedTextView.text = sharedPreferences.getString("US_INFECTED", "Unknown")
        vaccinatedTextView.text = sharedPreferences.getString("US_VACCINATED", "Unknown")
        deathsTextView.text = sharedPreferences.getString("US_DEATHS", "Unknown")

        newInfectedTextView.text = "+ ${sharedPreferences.getString("US_NEW_INFECTED", "Unknown")}"
        newVaccinatedTextView.text = "+ ${sharedPreferences.getString("US_NEW_VACCINATED", "Unknown")}"
        newDeathsTextView.text = "+ ${sharedPreferences.getString("US_NEW_DEATHS", "Unknown")}"
    }

    private fun setLineChart(liveChart: LiveChart, sharedPreferences: SharedPreferences, selected: String, stateSelected: String) {
        val visType : String = when (selected) {
            "Show Infected" -> "INFECTED"
            "Show Vaccinated" -> "VACCINATED"
            else -> "DEATHS"
        }

        val state : String = when (stateSelected) {
            "ALL STATES" -> "US"
            else -> stateSelected.replace("\\s".toRegex(), "")
        }

        // GET SIZE OF LIST
        val listSize = sharedPreferences.getInt("${state}_${visType}_LIST_SIZE", 0)

        // INITIALIZE EMPTY MUTABLE LIST
        val infectedCumulative : MutableList<DataPoint> = mutableListOf()
        val infected : MutableList<DataPoint> = mutableListOf()
        val date : MutableList<String> = mutableListOf()

        // ADD DATA POINTS
        for (i in 0 until listSize) {
            var sum = sharedPreferences.getString("${state}_${visType}_LIST_SUM_$i", "0")!!.toFloat()
            if (sum < 0) {
                sum = 0f
            }

            var value = (sharedPreferences.getString("${state}_${visType}_LIST_$i", "0")!!.toInt()).toFloat() * 70
            if (value < 0) {
                value = 0f
            }

            infectedCumulative.add(DataPoint(i.toFloat(), sum))
            infected.add(DataPoint(i.toFloat(), value)) // EXAGGERATE VALUE FOR BETTER GRAPH VISIBILITY
            date.add(sharedPreferences.getString("${state}_${visType}_LIST_DATE_$i", "No Date")!!)
        }

        val mainDataSet = Dataset(infected)
        liveChart
            .setDataset(mainDataSet)
            .setLiveChartStyle(getLineStyle(selected, state))
            .drawLastPointLabel()
            .setOnTouchCallbackListener(object : LiveChart.OnTouchCallback {
                override fun onTouchCallback(point: DataPoint) {
                    val i = point.x.roundToInt()

                    val date = sharedPreferences.getString("${state}_${visType}_LIST_DATE_$i", "No Date")!!.split("T")[0]
                    var sum = sharedPreferences.getString("${state}_${visType}_LIST_SUM_$i", "No Data")!!.toInt()
                    var daily = sharedPreferences.getString("${state}_${visType}_LIST_$i", "No Data")!!.toInt()

                    if (sum < 0) {
                        sum = 0
                    }

                    if (daily < 0) {
                        daily = 0
                    }

                    lineChartDateTextView.text = "Date: $date"
                    lineChartDailyTextView.text = "${visType.toLowerCase().capitalize()}: ${NumberFormat.getNumberInstance(Locale.US).format(daily)}"
                    lineChartSumTextView.text = "Total ${visType.toLowerCase()}: ${NumberFormat.getNumberInstance(Locale.US).format(sum)}"

                    lineChartDateTextView.setBackgroundColor(Color.WHITE)
                    lineChartDailyTextView.setBackgroundColor(Color.WHITE)
                    lineChartSumTextView.setBackgroundColor(Color.WHITE)

                    liveChart.parent.requestDisallowInterceptTouchEvent(true)
                }
                override fun onTouchFinished() {
                    lineChartDateTextView.text = ""
                    lineChartDailyTextView.text = ""
                    lineChartSumTextView.text = ""

                    lineChartDateTextView.setBackgroundColor(Color.TRANSPARENT)
                    lineChartDailyTextView.setBackgroundColor(Color.TRANSPARENT)
                    lineChartSumTextView.setBackgroundColor(Color.TRANSPARENT)
                    liveChart.parent.requestDisallowInterceptTouchEvent(false)
                }
            })
            .drawSmoothPath()
            .drawDataset()

        lineChartDateTextView.text = ""
        lineChartDailyTextView.text = ""
        lineChartSumTextView.text = ""
    }

    private fun getLineStyle(visType: String, state : String): LiveChartStyle {
        return LiveChartStyle().apply {

            val color : Int = when (visType) {
                "Show Infected" -> {
                    ContextCompat.getColor(this@MainActivity, R.color.orange);
                }
                "Show Vaccinated" -> {
                    ContextCompat.getColor(this@MainActivity, R.color.green);
                }
                else -> {
                    ContextCompat.getColor(this@MainActivity, R.color.red);
                }
            }

            secondColor = ContextCompat.getColor(this@MainActivity, R.color.gray);
            mainColor = color

            if (state == "US") {
                pathStrokeWidth = 2.5f
                secondPathStrokeWidth = 5f
            }
            else {

                pathStrokeWidth = 5f
                mainFillColor = color
            }

            overlayLineColor = ContextCompat.getColor(this@MainActivity, R.color.light_gray2);
            overlayCircleDiameter = 20f
            overlayCircleColor = ContextCompat.getColor(this@MainActivity, R.color.gray);
        }
    }
}

