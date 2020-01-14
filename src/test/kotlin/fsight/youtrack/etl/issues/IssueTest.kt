package fsight.youtrack.etl.issues

import org.junit.jupiter.api.Test

internal class IssueTest(private val service: IIssue) {

    @Test
    fun checkIfIssueExists() {
        service.checkIfIssueExists("TC-599", "TN")
    }
}