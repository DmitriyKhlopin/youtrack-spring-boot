@file:Suppress("DIVISION_BY_ZERO")

package fsight.youtrack


import fsight.youtrack.api.YouTrackAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class SimpleTest {
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

    @Tag("fast")
    @Test
    fun searchSingleIssue() {
        val q = listOf("idReadable").joinToString(",")
        val idReadable = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 100, skip = 0, query = "Состояние: {Направлена разработчику}").execute().body().orEmpty().firstOrNull()?.idReadable
        /*val idReadable = "IT-220"*/
        /*val issues = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 10, skip = 0, query = "${idReadable} Состояние: {Направлена разработчику} ").execute().body().orEmpty()
        */
        /**Поиск без # перед номером задачи выводит список задач вместо конкретной задачи.*//*
        assertNotEquals(1, issues.size, "Issue id was ${idReadable}")*/
        /**Для поиска конкретной задачи нужно добавлять # перед её номером.*/
        val issuesWithStrictFilter = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 10, skip = 0, query = "#${idReadable} Состояние: {Направлена разработчику} ").execute().body().orEmpty()
        assertEquals(1, issuesWithStrictFilter.size)
        assertEquals(idReadable, issuesWithStrictFilter.firstOrNull()?.idReadable)
    }
}