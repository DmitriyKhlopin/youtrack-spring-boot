@file:Suppress("DIVISION_BY_ZERO")

package fsight.youtrack


import fsight.youtrack.api.YouTrackAPI
import fsight.youtrack.models.unwrapFieldValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class SimpleTest {
    @Tag("fast")
    @Test
    fun issueParsingTest() {
        DatabaseTest()
        val idReadable = "TEST-1"
        val q = listOf("idReadable", "customFields(name,value(name))").joinToString(",")
        val request = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 100, skip = 0, query = "#$idReadable")
        val issue = request.execute().body().orEmpty().firstOrNull()
        assert(issue != null) { "Issue can't be null" }
        assertEquals(idReadable, issue?.idReadable, "Readable ids are not equal")
        assertEquals("Без подтверждения", issue?.unwrapFieldValue("State"), "States are not equal")
        assertEquals("Без подтверждения", issue?.unwrapFieldValue("Детализированное состояние"), "Detailed states are not equal")
        assertEquals("Внутренние работы", issue?.unwrapFieldValue("Заказчик"), "Customers are not equal")
        assertEquals("Normal", issue?.unwrapFieldValue("Priority"), "Priorities are not equal")
        //TODO сделать проверку заполненного значения
        assertEquals(null, issue?.unwrapFieldValue("Обоснование приоритета"), "Priority reasons are not equal")
        assertEquals("Консультация", issue?.unwrapFieldValue("Type"), "Types are not equal")
        assertEquals("FP 9.2", issue?.unwrapFieldValue("Продукт"), "Products are not equal")
        assertEquals("1. Отчетность", issue?.unwrapFieldValue("Subsystem"), "Module are not equal")
        //TODO сделать проверку для мультиселекта
        assertEquals(null, issue?.unwrapFieldValue("Affected versions"), "Affected versions are not equal")
        //TODO добавить кейсы для всех SLA
        assertEquals("Нет", issue?.unwrapFieldValue("SLA"), "SLAs are not equal")
        assertEquals("Нарушен", issue?.unwrapFieldValue("SLA по первому ответу"), "First response SLA states are not equal")
        assertEquals("1581068338908", issue?.unwrapFieldValue("Дата первого ответа"), "First response dates are not equal")
        assertEquals("Выполнен", issue?.unwrapFieldValue("SLA по решению"), "Solution SLA states are not equal")
        assertEquals("1580452650111", issue?.unwrapFieldValue("Дата решения"), "Solution dates are not equal")
        assertEquals(null, issue?.unwrapFieldValue("Issue"), "Issue ids are not equal")
        assertEquals(null, issue?.unwrapFieldValue("Requirement"), "Requirement ids are not equal")
        assertEquals("YouTrack", issue?.unwrapFieldValue("Источник запроса"), "Issue sources are not equal")
        assertEquals("Не оценена", issue?.unwrapFieldValue("Оценка"), "Evaluations are not equal")
        assertEquals("Не оценена", issue?.unwrapFieldValue("Внутренняя оценка"), "Reviews are not equal")
        assertEquals(null, issue?.unwrapFieldValue("Дата отслежиния"), "Tracing dates are not equal")
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

    @Tag("fast")
    @Test
    fun searchSingleIssue() {
        val q = listOf("idReadable").joinToString(",")
        val idReadable = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 100, skip = 0, query = "Состояние: {Направлена разработчику}").execute().body().orEmpty().firstOrNull()?.idReadable
        /*val idReadable = "IT-220"*/
        /*val issues = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 10, skip = 0, query = "${idReadable} Состояние: {Направлена разработчику} ").execute().body().orEmpty()
        *//**Поиск без # перед номером задачи выводит список задач вместо конкретной задачи.*//*
        assertNotEquals(1, issues.size, "Issue id was ${idReadable}")*/
        /**Для поиска конкретной задачи нужно добавлять # перед её номером.*/
        val issuesWithStrictFilter = YouTrackAPI.create(Converter.GSON).getIssueList(auth = AUTH, fields = q, top = 10, skip = 0, query = "#${idReadable} Состояние: {Направлена разработчику} ").execute().body().orEmpty()
        assertEquals(1, issuesWithStrictFilter.size)
        assertEquals(idReadable, issuesWithStrictFilter.firstOrNull()?.idReadable)
    }
}