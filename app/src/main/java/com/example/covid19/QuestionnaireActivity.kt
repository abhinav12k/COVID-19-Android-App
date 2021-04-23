package com.example.covid19

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.HtmlCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import org.jetbrains.anko.find
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.regex.Pattern

class QuestionnaireActivity : AppCompatActivity() {
    // Initialize Variables
    private lateinit var mainActivityButton: ImageView

    private lateinit var titleTextView: TextView

    private lateinit var progressBarLinearLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressCurrentTextView: TextView
    private lateinit var progressTotalTextView: TextView

    private lateinit var hashtagsTextView: TextView
    private lateinit var questionTextView: TextView

    private lateinit var inputLinearLayout: LinearLayout
    private lateinit var editText: EditText
    private lateinit var spinner: Spinner
    private lateinit var radioSex: RadioGroup
    private lateinit var editTextFeet: EditText
    private lateinit var editTextInches: EditText
    private lateinit var linearLayoutHeight: LinearLayout
    private lateinit var radioSmoked: RadioGroup
    private lateinit var radioSmoking: RadioGroup
    private lateinit var conditions: LinearLayout
    private lateinit var radioDiabetes: RadioGroup
    private lateinit var radioCancer: RadioGroup
    private lateinit var radioCancerHematological: RadioGroup
    private lateinit var radioCancerNonHematological: RadioGroup
    private lateinit var nextButton: Button

    private lateinit var resultLayout: LinearLayout
    private lateinit var result1textView: TextView
    private lateinit var result2textView: TextView

    private lateinit var zipcode: String

    private var cancer = false
    private var diabetes = false
    private var both = false

    private var risk = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show layout
        super.onCreate(savedInstanceState)
        theme.applyStyle(R.style.Theme_dark, true)
        setContentView(R.layout.activity_questionnaire)

        // Get layout components
        mainActivityButton = findViewById(R.id.btn_back_questionnaire)

        titleTextView = findViewById(R.id.title_tv)

        progressBarLinearLayout = findViewById(R.id.progress_bar)
        progressBar = findViewById(R.id.pb_progress)
        progressCurrentTextView = findViewById(R.id.tv_progress_current)
        progressTotalTextView = findViewById(R.id.tv_progress_total)

        hashtagsTextView = findViewById(R.id.hashtags_tv)
        questionTextView = findViewById(R.id.tv_question)

        inputLinearLayout = findViewById(R.id.input_layout)
        editText = findViewById(R.id.edit_text)
        spinner = findViewById(R.id.sp_ethnicityRace)
        radioSex = findViewById(R.id.radio_sex)
        editTextFeet = findViewById(R.id.edit_text_feet)
        editTextInches = findViewById(R.id.edit_text_inches)
        linearLayoutHeight = findViewById(R.id.height)
        radioSmoked = findViewById(R.id.radio_smoked)
        radioSmoking = findViewById(R.id.radio_smoking)
        conditions = findViewById(R.id.radio_conditions)
        radioDiabetes = findViewById(R.id.radio_diabetes)
        radioCancer = findViewById(R.id.radio_cancer)
        radioCancerHematological = findViewById(R.id.radio_cancer_hematological)
        radioCancerNonHematological = findViewById(R.id.radio_cancer_nonhematological)
        nextButton = findViewById(R.id.btn_next)

        resultLayout = findViewById(R.id.result_layout)

        result1textView = findViewById(R.id.result_tv_1)
        result2textView = findViewById(R.id.result_tv_2)

        // Main activity button listener
        mainActivityButton.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        // Next button listener
        nextButton.setOnClickListener(View.OnClickListener {
            // Get current question from progressBar
            val currentQuestion = progressBar.progress
            validateInput(currentQuestion)
        })

        // Remove title bar
        if (supportActionBar != null)
            supportActionBar?.hide()

        // Suppress dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Validates the input of the [currentQuestion], each questions need different validations a switch statement is used to handle all 15 questions
     */
    private fun validateInput(currentQuestion: Int) {
        when (currentQuestion) {
            1 -> {
                val input = if (editText.text.toString() == "") "0" else editText.text.toString()
                if (input.toInt() in 18..100) {
                    // Collect age
                    nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                val input = if (editText.text.toString() == "") "0" else editText.text.toString()
                val pattern = Pattern.compile("^[0-9]{5}(?:-[0-9]{4})?$")
                if (pattern.matcher(input).matches()) {
                    // Collect zip_code
                    zipcode = input
                    nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please enter a valid zip code", Toast.LENGTH_SHORT).show()
                }
            }
            3 -> {
                // Collect race_ethnicity
                nextQuestion(currentQuestion, 0)
            }
            4 -> {
                val selected = radioSex.checkedRadioButtonId
                if (selected != -1) {
                    // Collect sex
                    nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            5 -> {
                if (editTextFeet.text.isNotEmpty() && editTextInches.text.isNotEmpty()) {
                    // Collect height
                    nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            6 -> {
                if (editText.text.isNotEmpty()) {
                    // Collect weight
                    nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
                }
            }
            7 -> {
                val selected = radioSmoked.checkedRadioButtonId
                if (selected != -1) {
                    // Collect smoked
                    val checked = findViewById<RadioButton>(selected).text.toString()
                    if (checked == "No") nextQuestion(currentQuestion, 1)
                    if (checked == "Yes") nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            8 -> {
                val selected = radioSmoking.checkedRadioButtonId
                if (selected != -1) {
                    // Collect smoking
                    nextQuestion(currentQuestion, 0)
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            9 -> {
                for (i in 0 until conditions.childCount) {
                    // Collect conditions
                    val radio = findViewById<RadioButton>(conditions.getChildAt(i).id)
                    val text = radio.text.toString()
                    if (text == "Diabetes") diabetes = radio.isChecked
                    if (text == "Any Cancer") cancer = radio.isChecked
                }

                if (diabetes) nextQuestion(currentQuestion, 0)
                else if (cancer) nextQuestion(currentQuestion, 1)
                else showResult()
            }
            10 -> {
                val selected = radioDiabetes.checkedRadioButtonId
                if (selected != -1) {
                    // Collect diabetes_type
                    if (cancer) nextQuestion(currentQuestion, 0)
                    if (!cancer) showResult()
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            11 -> {
                val selected = radioCancer.checkedRadioButtonId
                if (selected != -1) {
                    val checked = findViewById<RadioButton>(selected).text.toString()
                    // Collect cancer_type
                    if (checked == "Non-Hematological") nextQuestion(currentQuestion, 1)
                    if (checked == "Hematological") {
                        nextQuestion(currentQuestion, 0)
                    }
                    if (checked == "Both") {
                        both = true
                        nextQuestion(currentQuestion, 0)
                    }
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            12 -> {
                val selected = radioCancerHematological.checkedRadioButtonId
                if (selected != -1) {
                    // Collect cancer_hematological_diagnosed
                    if (both) nextQuestion(currentQuestion, 0)
                    if (!both) nextQuestion(currentQuestion, 1)
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
            13 -> {
                val selected = radioCancerNonHematological.checkedRadioButtonId
                if (selected != -1) {
                    // Collect cancer_nonhematological_diagnosed
                    showResult()
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Calculates what is the next question based on the [currentQuestion] and if a question needs to be [skip]ped
     * This function is called after validating the input in the [validateInput] function
     */
    private fun nextQuestion(currentQuestion: Int, skip: Int) {
        if (currentQuestion <= progressBar.max) {
            progressBar.progress = progressBar.progress + skip + 1
            progressCurrentTextView.text = (currentQuestion + skip + 1).toString()
            setNextQuestion(currentQuestion + skip)
        }
    }

    /**
     * Prepares the next question layout based on the [currentQuestion], mainly hides and shows components from the layout with the
     * component.isVisible property, it also reuses some component by changing their properties defaults
     */
    private fun setNextQuestion(currentQuestion: Int) {
        // Hide keyboard
        this.currentFocus?.let { view ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        when (currentQuestion) {
            1 -> {
                editText.filters = arrayOf(InputFilter.LengthFilter(5))
                editText.hint = getString(R.string.q2hint)
                editText.setText(getString(R.string.blank))
                questionTextView.text = getString(R.string.q2)
            }
            2 -> {
                val spinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
                    this,
                    R.array.ethnicityRace,
                    R.layout.spinner_questionnaire_item
                )
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_questionnaire_dropdown_item)

                editText.isVisible = false
                spinner.isVisible = true
                spinner.adapter = spinnerAdapter
                questionTextView.text = getString(R.string.q3)
            }
            3 -> {
                spinner.isVisible = false
                showRadioGroup(radioSex, true)
                questionTextView.text = getString(R.string.q4)
            }
            4 -> {
                showRadioGroup(radioSex, false)
                linearLayoutHeight.isVisible = true
                questionTextView.text = getString(R.string.q5)
            }
            5 -> {
                linearLayoutHeight.isVisible = false
                editText.isVisible = true
                editText.filters = arrayOf(InputFilter.LengthFilter(4))
                editText.hint = getString(R.string.q6hint)
                editText.setText(getString(R.string.blank))
                questionTextView.text = getString(R.string.q6)
            }
            6 -> {
                editText.isVisible = false
                showRadioGroup(radioSmoked, true)
                questionTextView.text = getString(R.string.q7)
            }
            7 -> {
                showRadioGroup(radioSmoked, false)
                showRadioGroup(radioSmoking, true)
                questionTextView.text = getString(R.string.q8)
            }
            8 -> {
                showRadioGroup(radioSmoked, false)
                showRadioGroup(radioSmoking, false)
                showConditions(conditions,true)
                questionTextView.text = getString(R.string.q9)
            }
            9 -> {
                showConditions(conditions, false)
                showRadioGroup(radioDiabetes, true)
                questionTextView.text = getString(R.string.q10)
            }
            10 -> {
                showConditions(conditions, false)
                showRadioGroup(radioDiabetes, false)
                showRadioGroup(radioCancer, true)
                questionTextView.text = getString(R.string.q11)
            }
            11 -> {
                showConditions(conditions, false)
                showRadioGroup(radioDiabetes, false)
                showRadioGroup(radioCancer, false)
                showRadioGroup(radioCancerHematological, true)
                questionTextView.text = getString(R.string.q12)
            }
            12 -> {
                showConditions(conditions, false)
                showRadioGroup(radioDiabetes, false)
                showRadioGroup(radioCancer, false)
                showRadioGroup(radioCancerHematological, false)
                showRadioGroup(radioCancerNonHematological, true)
                questionTextView.text = getString(R.string.q13)
            }
        }
    }

    /**
     * Hides or shows a [radioGroup] based on the boolean [show]
     */
    private fun showRadioGroup(radioGroup: RadioGroup, show: Boolean) {
        radioGroup.forEach { radio ->
            radio.isVisible = show
        }
    }

    /**
     * Hides or shows all children inside a [LinearLayout] based on the boolean [show]
     */
    private fun showConditions(linearLayout: LinearLayout, show: Boolean) {
        for (i in 0 until linearLayout.childCount) {
            linearLayout.getChildAt(i).isVisible = show
        }
    }

    /**
     * Hides all non required components, reads the risktoolsamples CSV file and searches a match with the given values, then show and format the results.
     */
    private fun showResult() {
        inputLinearLayout.isVisible = false
        progressBarLinearLayout.isVisible = false
        hashtagsTextView.isVisible = false
        questionTextView.isVisible = false
        nextButton.isVisible = false

        titleTextView.text = getString(R.string.results)

        // Reads CSV risktoolsamples and matches ZIP code and add results to a mutableList
        val inputStream = resources.openRawResource(R.raw.risktoolsamples)
        val bufferReader =
            BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        val iterator = bufferReader.lineSequence().iterator()
        while (iterator.hasNext()) {
            val line = iterator.next().split(",")
            if (line[1] == zipcode) {
                for (i in 24..32) {
                    risk.add(line[i])
                }
            }
        }

        // To avoid crash when no matches found assign a default value to risk[0] to risk[8]
        if (risk.isEmpty()) {
            for (i in 0..8) {
                risk[i] = "-"
            }
        }

        result1textView.text = Html.fromHtml(String.format(getString(R.string.risk_result1), risk[0], risk[1], risk[2], risk[3]), HtmlCompat.FROM_HTML_MODE_LEGACY)
        result2textView.text = Html.fromHtml(String.format(getString(R.string.risk_result2), risk[4], risk[5], risk[6], risk[7], risk[8]), HtmlCompat.FROM_HTML_MODE_LEGACY)
        resultLayout.isVisible = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        this.finish()
    }
}