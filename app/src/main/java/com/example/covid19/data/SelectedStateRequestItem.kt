package com.example.covid19.data

data class SelectedStateRequestItem(
        val checkTimeEt: String?,
        val commercialScore: Int?,
        val dataQualityGrade: Any?,
        val date: Int?,
        val dateChecked: String?,
        val dateModified: String?,
        val death: Int?,
        val deathConfirmed: Int?,
        val deathIncrease: Int?,
        val deathProbable: Any?,
        val fips: String?,
        val grade: String?,
        val hash: String?,
        val hospitalized: Int?,
        val hospitalizedCumulative: Int?,
        val hospitalizedCurrently: Int?,
        val hospitalizedDischarged: Int?,
        val hospitalizedIncrease: Int?,
        val inIcuCumulative: Any?,
        val inIcuCurrently: Int?,
        val lastUpdateEt: String?,
        val negative: Any?,
        val negativeIncrease: Int?,
        val negativeRegularScore: Int?,
        val negativeScore: Int?,
        val negativeTestsAntibody: Any?,
        val negativeTestsPeopleAntibody: Any?,
        val negativeTestsViral: Any?,
        val onVentilatorCumulative: Any?,
        val onVentilatorCurrently: Int?,
        val pending: Int?,
        val posNeg: Int?,
        val positive: Int?,
        val positiveCasesViral: Any?,
        val positiveIncrease: Int?,
        val positiveScore: Int?,
        val positiveTestsAntibody: Any?,
        val positiveTestsAntigen: Any?,
        val positiveTestsPeopleAntibody: Any?,
        val positiveTestsPeopleAntigen: Any?,
        val positiveTestsViral: Any?,
        val probableCases: Any?,
        val recovered: Any?,
        val score: Int?,
        val state: String?,
        val total: Int?,
        val totalTestEncountersViral: Int?,
        val totalTestResults: Int?,
        val totalTestResultsIncrease: Int?,
        val totalTestResultsSource: String?,
        val totalTestsAntibody: Any?,
        val totalTestsAntigen: Any?,
        val totalTestsPeopleAntibody: Any?,
        val totalTestsPeopleAntigen: Any?,
        val totalTestsPeopleViral: Any?,
        val totalTestsViral: Any?
)