package fsight.youtrack.models.youtrack

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.models.YouTrackActivityCursor
import fsight.youtrack.models.toIssueHistoryRecord
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils
import java.io.File

internal class YouTrackActivityTest {
    @Tag("fast")
    @Test
    fun aTest() {
        val file: File = ResourceUtils.getFile("classpath:test/issues/issuesHistoryParsing.json")
        assert(file.exists())
        val history: YouTrackActivityCursor = Gson().fromJson(String(file.readBytes()), object : TypeToken<YouTrackActivityCursor>() {}.type)
        history.activities?.forEach {
            println(it.added)
            println(it.removed)
        }
        history.activities?.filter{it.field?.customField?.fieldType?.valueType == "version"}?.map { it.toIssueHistoryRecord("") }?.forEach { println(it) }
    }
}
