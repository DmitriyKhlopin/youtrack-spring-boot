package fsight.youtrack


import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.models.youtrack.StateCustomFieldValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class SimpleTest {
    @Tag("fast")
    @Test
    fun basicAssertion() {
        val idReadable = "SA-341"
        val q = listOf("idReadable", "fields(name,value(name))").joinToString(",")
        val request = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 100, skip = 0, query = idReadable)
        val issue = request.execute().body().orEmpty().firstOrNull()
        assert(issue != null) { "Issue can't be null" }
        assertEquals(idReadable, issue?.idReadable, "Readable ids are not equal")
        assertEquals(idReadable, (issue?.customFields?.firstOrNull { it.`$type` == "StateBundleElement" }?.value as StateCustomFieldValue).name, "Readable ids are not equal")
    }

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource(
            "0,    1,   1",
            "1,    2,   3",
            "49,  51, 100",
            "1,  100, 101"
    )
    fun add(first: Int, second: Int, expectedResult: Int) {
        assertEquals(expectedResult, first + second) {
            "$first + $second should equal $expectedResult"
        }
    }

    @Test
    fun divisionByZeroError() {
        val exception = assertThrows<ArithmeticException> {
            1 / 0
        }
        assertEquals("/ by zero", exception.message)
    }
}