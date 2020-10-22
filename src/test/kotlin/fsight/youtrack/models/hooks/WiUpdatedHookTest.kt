package fsight.youtrack.models.hooks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.util.ResourceUtils
import java.io.File

internal class WiUpdatedHookTest {

    @Test
    fun wasIncludedToSprint() {
        val file: File = ResourceUtils.getFile("classpath:test/hooks/includedToSprint.json")
        assert(file.exists())
        val wiUpdatedHook: WiUpdatedHook = Gson().fromJson(String(file.readBytes()), object : TypeToken<WiUpdatedHook>() {}.type)
        assertTrue(wiUpdatedHook.wasIncludedToSprint(), "Bug was not included to sprint")
    }
}
