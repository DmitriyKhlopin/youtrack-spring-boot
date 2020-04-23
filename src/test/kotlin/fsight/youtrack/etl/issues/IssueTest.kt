package fsight.youtrack.etl.issues

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.etl.ETLState
import fsight.youtrack.etl.logs.ImportLog
import fsight.youtrack.etl.timeline.Timeline
import fsight.youtrack.models.hooks.Hook
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils
import java.io.File


class IssueTest {


    private val db: DSLContext by lazy {
        DSL.using("jdbc:postgresql://v-ctp-postgres.fs.fsight.world:5432/youtrack?autoReconnect=true", "postgres", "Qwerty1")

    }

    @Test
    fun checkIfIssueExists() {
        println("this is a test")
        val service: IIssue = Issue(db, ImportLog(db), Timeline(db), ETLState())

        service.checkIfIssueExists("TC-599", "TN")
        service.getIssues("#ACGM-11")
        assert(true)
    }


}
