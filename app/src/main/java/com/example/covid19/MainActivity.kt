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
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Map
import com.anychart.charts.Pie
import com.anychart.core.map.series.Choropleth
import com.anychart.core.ui.ColorRange
import com.anychart.enums.SelectionMode
import com.anychart.enums.SidePosition
import com.anychart.graphics.vector.text.HAlign
import com.anychart.scales.LinearColor
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

    private lateinit var infectedLinearLayout: LinearLayout
    private lateinit var vaccinatedLinearLayout: LinearLayout
    private lateinit var deathsLinearLayout: LinearLayout

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

    private lateinit var lineChartTextView : TextView

    private lateinit var lineChart : LiveChart

    private lateinit var lineChartDateTextView: TextView
    private lateinit var lineChartDailyTextView: TextView
    private lateinit var lineChartSumTextView: TextView

    private lateinit var mapAnyChartView: AnyChartView

    private var visSelected = "INFECTED"
    private var stateSelected = "US"
    private var codeSelected = "US"

    /**
     * It does a lot of stuff, read comments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // SHOW LAYOUT
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_white)
        setContentView(R.layout.activity_main)

        // GET COMPONENTS
        questionnaireCardView = findViewById(R.id.cv_questionnaire_button)

        statesSpinner = findViewById(R.id.sp_states)

        infectedLinearLayout = findViewById(R.id.cv_infected)
        vaccinatedLinearLayout = findViewById(R.id.cv_vaccinated)
        deathsLinearLayout = findViewById(R.id.cv_deaths)

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

        lineChartTextView = findViewById(R.id.tv_vistype)

        lineChart = findViewById(R.id.line_chart)
        lineChartDateTextView = findViewById(R.id.live_chart_date)
        lineChartSumTextView = findViewById(R.id.live_chart_sum)
        lineChartDailyTextView = findViewById(R.id.live_chart_daily)

        mapAnyChartView = findViewById(R.id.map)

        // ACCESS PHONE'S SHARED PREFERENCES
        val sharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE);

        // SET TODAY'S DATE TEXT
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(calendar.time)
        dateTextView.text = date

        // SPINNER AND SPINNER ITEM STYLING AND DECLARATION
        // STATES SPINNER
        val statesSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this,
            R.array.states, R.layout.spinner_states_item
        )
        statesSpinnerAdapter.setDropDownViewResource(R.layout.spinner_states_dropdown_item)
        statesSpinner.adapter = statesSpinnerAdapter

        // STATE SPINNER LISTENER
        statesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // GET SELECTED STATE
                val selected = adapterView?.getItemAtPosition(position).toString().toUpperCase(
                    Locale.ROOT
                )

                // UPDATE GLOBAL VARIABLES
                stateSelected = selected
                codeSelected = States().getStatesMap()[adapterView?.getItemAtPosition(position)].toString()

                // INITIALIZE STRINGS
                val selectedFormatted: String

                if (selected != "ALL STATES") {
                    // FORMAT SELECTED FROM "NEW YORK" TO "NEWYORK"
                    selectedFormatted = selected.replace(" ", "")

                    // SET TOTAL NUMBERS TEXT
                    latestUpdateTextView.text = sharedPreferences.getString(
                        "${selectedFormatted}_UPDATED",
                        "Unknown"
                    )
                    infectedTextView.text = sharedPreferences.getString(
                        "${selectedFormatted}_INFECTED",
                        "Unknown"
                    )
                    vaccinatedTextView.text = sharedPreferences.getString(
                        "${selectedFormatted}_VACCINATED",
                        "Unknown"
                    )
                    deathsTextView.text = sharedPreferences.getString(
                        "${selectedFormatted}_DEATHS",
                        "Unknown"
                    )

                    // GET NEW NUMBERS
                    val newInfected = sharedPreferences.getString(
                        "${selectedFormatted}_NEW_INFECTED",
                        "0"
                    )
                    val newVaccinated = sharedPreferences.getString(
                        "${selectedFormatted}_NEW_VACCINATED",
                        "0"
                    )
                    val newDeaths = sharedPreferences.getString(
                        "${selectedFormatted}_NEW_DEATHS",
                        "0"
                    )

                    // FORMAT NEW NUMBERS
                    val formattedNewInfected = if (newInfected!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newInfected"
                    val formattedNewVaccinated = if (newVaccinated!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newVaccinated"
                    val formattedNewDeaths = if (newDeaths!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newDeaths"

                    // SET NEW NUMBERS TEXT
                    newInfectedTextView.text = formattedNewInfected
                    newVaccinatedTextView.text = formattedNewVaccinated
                    newDeathsTextView.text = formattedNewDeaths
                } else {
                    // UPDATE GLOBAL VARIABLES
                    codeSelected = "US"

                    // SET U.S DATA
                    setDefaultData(sharedPreferences)
                }

                // SET SUB-TITLES TEXT
                val lineChartText ="$codeSelected ${visSelected.toLowerCase(Locale.ROOT).capitalize(
                    Locale.ROOT
                )}"
                val latestUpdateTileText = "$codeSelected Cases Overview"
                lineChartTextView.text = lineChartText
                latestUpdateTitleTextView.text = latestUpdateTileText

                // DRAW LINE CHART
                setLineChart(lineChart, sharedPreferences, stateSelected)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // IGNORE: THERE IS ALWAYS AN ELEMENT SELECTED
            }
        }

        // INFECTED CARD LISTENER
        infectedLinearLayout.setOnClickListener(View.OnClickListener {
            // SET GLOBAL VARIABLE
            visSelected = "INFECTED"
            // SET SUB-TITLE TEXT
            val lineChartText = "$codeSelected ${
                visSelected.toLowerCase(Locale.ROOT).capitalize(
                    Locale.ROOT
                )
            }"
            lineChartTextView.text = lineChartText
            // DRAW LINE CHART
            setLineChart(lineChart, sharedPreferences, stateSelected)
        })

        // VACCINATED CARD LISTENER
        vaccinatedLinearLayout.setOnClickListener(View.OnClickListener {
            // SET GLOBAL VARIABLE
            visSelected = "VACCINATED"
            // SET SUB-TITLE TEXT
            val lineChartText = "$codeSelected ${
                visSelected.toLowerCase(Locale.ROOT).capitalize(
                    Locale.ROOT
                )
            }"
            lineChartTextView.text = lineChartText
            // DRAW LINE CHART
            setLineChart(lineChart, sharedPreferences, stateSelected)
        })

        // DEATHS CARD LISTENER
        deathsLinearLayout.setOnClickListener(View.OnClickListener {
            // SET GLOBAL VARIABLE
            visSelected = "DEATHS"
            // SET SUB-TITLE TEXT
            val lineChartText = "$codeSelected ${
                visSelected.toLowerCase(Locale.ROOT).capitalize(
                    Locale.ROOT
                )
            }"
            lineChartTextView.text = lineChartText
            // DRAW LINE CHART
            setLineChart(lineChart, sharedPreferences, stateSelected)
        })

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
        val s = HtmlCompat.fromHtml(
            "<a href='https://covid.cdc.gov/covid-data-tracker/#datatracker-home'>Details</a>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0)
        }
        detailsLinkTextView.text = s
        // OPEN CDC HYPER-LINK
        detailsLinkTextView.movementMethod = LinkMovementMethod.getInstance()

        // REMOVE TITLE BAR
        if (supportActionBar != null)
            supportActionBar?.hide()

        // SUPPRESS DARK MODE
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setChart()
    }

    private fun setChart() {
        val pie : Pie = AnyChart.pie()
        val data = arrayListOf<DataEntry>()

        data.add(ValueDataEntry("A", 100))
        data.add(ValueDataEntry("B", 120))
        data.add(ValueDataEntry("C", 180))

        pie.data(data)
        mapAnyChartView.setChart(pie)
    }


    /**
     * Sets the text for various TextViews with U.S COVID-19 data based on what's found on [sharedPreferences].
     */
    private fun setDefaultData(sharedPreferences: SharedPreferences) {
        // SET TOTAL NUMBERS
        latestUpdateTextView.text = sharedPreferences.getString("US_UPDATED", "Unknown")
        infectedTextView.text = sharedPreferences.getString("US_INFECTED", "Unknown")
        vaccinatedTextView.text = sharedPreferences.getString("US_VACCINATED", "Unknown")
        deathsTextView.text = sharedPreferences.getString("US_DEATHS", "Unknown")

        // SET NEW NUMBERS
        val newInfected = "+ ${sharedPreferences.getString("US_NEW_INFECTED", "Unknown")}"
        val newVaccinated = "+ ${sharedPreferences.getString("US_NEW_VACCINATED", "Unknown")}"
        val newDeaths = "+ ${sharedPreferences.getString("US_NEW_DEATHS", "Unknown")}"

        newInfectedTextView.text = newInfected
        newVaccinatedTextView.text = newVaccinated
        newDeathsTextView.text = newDeaths
    }

    /**
     * Draws the [liveChart] based on the Global variable [visSelected] visualization and the [stateSelected] by retrieving COVID-19 data from the phone's [sharedPreferences].
     */
    private fun setLineChart(
        liveChart: LiveChart,
        sharedPreferences: SharedPreferences,
        stateSelected: String
    ) {
        // INITIALIZE EMPTY MUTABLE LISTS TO STORE DATA POINTS AND DATES
        val dateRecorded : MutableList<String> = mutableListOf()
        val cumulativeRecorded : MutableList<DataPoint> = mutableListOf()
        val recorded : MutableList<DataPoint> = mutableListOf()

        // FORMAT SELECTED STATE FROM "NEW YORK" to "NEWYORK" or "ALL STATES" to "US"
        val state : String = when (stateSelected) {
            "ALL STATES" -> "US"
            else -> stateSelected.replace(" ".toRegex(), "")
        }

        // GET SIZE OF LIST
        val listSize = sharedPreferences.getInt("${state}_${visSelected}_LIST_SIZE", 0)

        // ADD DATA FOUND IN SHARED PREFERENCES TO MUTABLE LISTS
        // SKIP FIRST DATA POINT IN INFECTED AND DEATHS SINCE IT SHOWS NUMBERS UNTIL THE FIRST DATA API STARTED RECORDING NUMBERS
        if (visSelected != "VACCINATED") {
            for (i in 1 until listSize) {
                dateRecorded.add(
                    sharedPreferences.getString(
                        "${state}_${visSelected}_LIST_DATE_$i",
                        "No Date"
                    )!!
                )

                // REMOVE NEGATIVE NUMBERS
                val nonNegativeRecorded =
                    if (sharedPreferences.getString("${state}_${visSelected}_LIST_$i", "0")!!.toFloat() >= 0f)
                        sharedPreferences.getString("${state}_${visSelected}_LIST_$i", "0")!!.toFloat() else 0f

                cumulativeRecorded.add(
                    DataPoint(
                        i.toFloat(), sharedPreferences.getString(
                            "${state}_${visSelected}_LIST_SUM_$i",
                            "0"
                        )!!.toFloat()
                    )
                )
                recorded.add(DataPoint(i.toFloat(), nonNegativeRecorded))
            }
        } else {
            for (i in 0 until listSize) {
                dateRecorded.add(
                    sharedPreferences.getString(
                        "${state}_${visSelected}_LIST_DATE_$i",
                        "No Date"
                    )!!
                )
                cumulativeRecorded.add(
                    DataPoint(
                        i.toFloat(), sharedPreferences.getString(
                            "${state}_${visSelected}_LIST_SUM_$i",
                            "0"
                        )!!.toFloat()
                    )
                )
                recorded.add(
                    DataPoint(
                        i.toFloat(), sharedPreferences.getString(
                            "${state}_${visSelected}_LIST_$i",
                            "0"
                        )!!.toFloat()
                    )
                )
            }
        }


        // INITIALIZE DATA SET
        val mainDataSet = Dataset(recorded)

        // DRAW LINE CHART
        liveChart
            .setDataset(mainDataSet) // WITH DATA SET
            .setLiveChartStyle(getLineStyle()) //WITH STYLE
            .setOnTouchCallbackListener(object : LiveChart.OnTouchCallback {
                /**
                 * Sets the text inside the [liveChart] with matching from the line based on what's found on [sharedPreferences] when touching the [liveChart] by using [point].x as index.
                 */
                override fun onTouchCallback(point: DataPoint) {
                    // CONVERT POINT.x FLOAT TO INT
                    val i = point.x.roundToInt()

                    // SET TEXTVIEW TEXT
                    val lineChartDateText = "Date: ${
                        sharedPreferences.getString(
                            "${state}_${visSelected}_LIST_DATE_$i",
                            "No Date"
                        )!!.split("T")[0]
                    }"
                    val lineChartSumText = "Total ${visSelected.toLowerCase(Locale.ROOT)}: ${
                        NumberFormat.getNumberInstance(
                            Locale.US
                        ).format(
                            sharedPreferences.getString(
                                "${state}_${visSelected}_LIST_SUM_$i",
                                "0"
                            )!!.toInt()
                        )
                    }"
                    val lineChartDailyText = "${
                        visSelected.toLowerCase(Locale.ROOT).capitalize(
                            Locale.ROOT
                        )
                    }: ${
                        NumberFormat.getNumberInstance(Locale.US).format(
                            sharedPreferences.getString(
                                "${state}_${visSelected}_LIST_$i",
                                "0"
                            )!!.toInt()
                        )
                    }"

                    lineChartDateTextView.text = lineChartDateText
                    lineChartSumTextView.text = lineChartSumText
                    lineChartDailyTextView.text = lineChartDailyText

                    // SET TEXTVIEW BACKGROUND COLOR
                    lineChartDateTextView.setBackgroundColor(Color.WHITE)
                    lineChartSumTextView.setBackgroundColor(Color.WHITE)
                    lineChartDailyTextView.setBackgroundColor(Color.WHITE)

                    liveChart.parent.requestDisallowInterceptTouchEvent(true)
                }

                /**
                 * Empties the [liveChart] TextViews when finished touching the [liveChart].
                 */
                override fun onTouchFinished() {
                    // SET TEXTVIEW TEXT
                    lineChartDateTextView.text = ""
                    lineChartSumTextView.text = ""
                    lineChartDailyTextView.text = ""

                    // SET TEXTVIEW BACKGROUND COLOR
                    lineChartDateTextView.setBackgroundColor(Color.TRANSPARENT)
                    lineChartSumTextView.setBackgroundColor(Color.TRANSPARENT)
                    lineChartDailyTextView.setBackgroundColor(Color.TRANSPARENT)

                    liveChart.parent.requestDisallowInterceptTouchEvent(false)
                }
            })
            .drawLastPointLabel()
            .drawBaseline()
            .setBaselineManually(recorded[recorded.size - 1].y)
            .drawSmoothPath()
            .drawDataset()
    }

    /**
     * Returns a LiveChartStyle based on the GLOBAL VARIABLE [visSelected]
     */
    private fun getLineStyle(): LiveChartStyle {
        return LiveChartStyle().apply {
            // COLORS
            mainColor = when (visSelected) {
                "INFECTED" -> {
                    ContextCompat.getColor(this@MainActivity, R.color.orange);
                }
                "VACCINATED" -> {
                    ContextCompat.getColor(this@MainActivity, R.color.green);
                }
                else -> {
                    ContextCompat.getColor(this@MainActivity, R.color.red);
                }
            }
            overlayLineColor = ContextCompat.getColor(this@MainActivity, R.color.light_gray2)
            overlayCircleColor = ContextCompat.getColor(this@MainActivity, R.color.gray)
            baselineColor = ContextCompat.getColor(this@MainActivity, R.color.light_gray2)

            // WIDTH AND CIRCLE DIAMETER
            pathStrokeWidth = 4f
            baselineStrokeWidth = 4f
            overlayCircleDiameter = 10f
        }
    }
}

