package fsight.youtrack.etl.issues

import fsight.youtrack.etl.ETLState
import fsight.youtrack.etl.logs.ImportLog
import fsight.youtrack.etl.timeline.Timeline
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test


class IssueTest {


    private val db: DSLContext by lazy {
        DSL.using("jdbc:postgresql://v-ctp-postgres.fs.fsight.world:5432/youtrack?autoReconnect=true", "postgres", "Qwerty1")

    }

    @Test
    fun checkIfIssueExists() {
        println("this is a test")
        val service: IIssue = Issue(db, ImportLog(db),   ETLState())

        service.checkIfIssueExists("TC-599", "TN")
        service.getIssues("#ACGM-11")
        assert(true)
    }


}
