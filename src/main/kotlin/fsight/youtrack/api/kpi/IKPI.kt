package fsight.youtrack.api.kpi

import java.sql.Timestamp

interface IKPI {
    fun getResult(projects: List<String>, emails: List<String>, dateFrom: Timestamp, dateTo: Timestamp): List<KPI.R1>
    fun getTotal(projects: List<String>, emails: List<String>, dateFrom: Timestamp, dateTo: Timestamp): List<KPI.T1>
    fun getTotal2(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): Map<String, Int>

    fun getViolations(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getPostponements(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getSuggestedSolutions(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getRequestedClarifications(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getPerformanceEvaluations(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getCommercialUtilization(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getIssueTimeUtilization(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getFoundErrors(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getSelfSolved(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): List<KPI.T1>

    fun getResult2(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp,
            withDetails: Boolean
    ): Any

    fun getOverallResult(
            projects: List<String>,
            emails: List<String>,
            dateFrom: Timestamp,
            dateTo: Timestamp
    ): Any
}
