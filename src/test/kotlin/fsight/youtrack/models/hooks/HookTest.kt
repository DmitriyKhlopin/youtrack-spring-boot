package fsight.youtrack.models.hooks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fsight.youtrack.ExposedTransformations
import fsight.youtrack.db.exposed.helper.Queries
import fsight.youtrack.execAndMap
import fsight.youtrack.models.DevOpsBugState
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.util.ResourceUtils
import java.io.File

internal class HookTest {

    @Test
    fun wasIncludedToSprint() {
        val file: File = ResourceUtils.getFile("classpath:test/hooks/includedToSprint.json")
        assert(file.exists())
        val hook: Hook = Gson().fromJson(String(file.readBytes()), object : TypeToken<Hook>() {}.type)
        assertTrue(hook.wasIncludedToSprint(), "Bug was not included to sprint")
    }
}