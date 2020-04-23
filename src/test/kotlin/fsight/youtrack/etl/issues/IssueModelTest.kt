package fsight.youtrack.etl.issues

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.models.youtrack.Issue
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils
import java.io.File

class IssueModelTest {
    @Test
    fun parseCustomFields() {
        val file: File = ResourceUtils.getFile("classpath:test/issues/customFieldsParsing.json")
        assert(file.exists())
        val body: Issue = Gson().fromJson(String(file.readBytes()), object : TypeToken<Issue>() {}.type)
        println(body)
        body.customFields?.forEach {
            println(it)
            println(body.unwrapFieldValue(it.projectCustomField?.field?.name))
        }
    }
}