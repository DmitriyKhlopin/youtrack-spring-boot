package fsight.youtrack

import org.springframework.http.HttpHeaders

enum class HookTypes {
    CHANGE,
    COMMENT
}

enum class LogType(val type: String) {
    DebugView("log"),
    DebugViewPlusPlus("dblog"),
    DEFAULT("unresolved");

    companion object {
        fun from(type: String?): LogType = values().find { it.type == type } ?: DEFAULT
    }
}


enum class ETLState(val state: Int) {
    IDLE(0),
    RUNNING(1),
    DONE(2)
}

enum class Converter {
    GSON, SCALAR
}

fun headers(): HttpHeaders {
    val h = HttpHeaders()
    h.add("Content-Type", "application/json; charset=utf-8")
    return h
}

const val TEST_HOOK_ID = "00000000-0000-0000-0000-000000000000"

const val PRINT = true

const val REACT_STATIC_DIR = "static"
const val REACT_DIR = "/static/"

const val ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.ActivityItemImpl"
const val COMMENT_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.CommentActivityItem"
const val MARKUP_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.MarkupActivityItem"
const val TIME_TRACKING_ACTIVITY_ITEM = "jetbrains.youtrack.timetracking.event.TimeTrackingActivityItem"
const val ATTACHMENT_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.AttachmentActivityItem"
const val ISSUE_CREATED_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.IssueCreatedActivityItem"
const val PROJECT_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.ProjectActivityItem"
const val TEXT_FIELD_ACTIVITY_ITEM = "jetbrains.youtrack.event.category.custom.TextFieldMarkupActivityItem"
const val VISIBILITY_GROUP_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.VisibilityGroupActivityItem"
const val VISIBILITY_USER_ACTIVITY_ITEM = "jetbrains.youtrack.event.gaprest.impl.VisibilityUserActivityItem"


const val SINGLE_ENUM_ISSUE_CUSTOM_FIELD = "SingleEnumIssueCustomField"
const val SINGLE_USER_ISSUE_CUSTOM_FIELD = "SingleUserIssueCustomField"
const val STATE_ISSUE_CUSTOM_FIELD = "StateIssueCustomField"
const val SIMPLE_ISSUE_CUSTOM_FIELD = "SimpleIssueCustomField"
const val SINGLE_VERSION_ISSUE_CUSTOM_FIELD = "SingleVersionIssueCustomField"
const val SINGLE_BUILD_ISSUE_CUSTOM_FIELD = "SingleBuildIssueCustomField"
const val DATE_ISSUE_CUSTOM_FIELD = "DateIssueCustomField"
const val TEXT_ISSUE_CUSTOM_FIELD = "TextIssueCustomField"
const val PERIOD_ISSUE_CUSTOM_FIELD = "PeriodIssueCustomField"
const val STATE_MACHINE_ISSUE_CUSTOM_FIELD = "StateMachineIssueCustomField"
const val MULTI_ENUM_ISSUE_CUSTOM_FIELD = "MultiEnumIssueCustomField"
const val SINGLE_OWNED_ISSUE_CUSTOM_FIELD = "SingleOwnedIssueCustomField"
const val MULTI_VERSION_ISSUE_CUSTOM_FIELD = "MultiVersionIssueCustomField"

const val defaultProjects = ""

const val defaultUsersWithLeads =
    "iana.maltseva@fsight.ru,viktoriya.zolotaryova@fsight.ru,artem.maltsev@fsight.ru,dmitriy.khlopin@fsight.ru,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,andrey.nepomnyashchiy@fsight.ru,nikolai.parkhachev@fsight.ru"
const val defaultUsers =
    "iana.maltseva@fsight.ru,dmitriy.khlopin@fsight.ru,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,nikolai.parkhachev@fsight.ru,olga.subbotina@fsight.ru"

val htmlHead = """
         <head>
    <meta charset="UTF-8">
    <title>Title</title>
    <style type="text/css">
        body {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            color: #1f2326;
        }

        table {
            border-collapse: collapse;
            border: 1px solid black;
        }

        th, td {
            padding: 6px 12px;
            text-align: left;
            border: 1px solid black;
        }

        tr {
            padding: 0
        }
    </style>
</head>
    """.trimIndent()

val errorsTableHeader = """
        <tr>
            <th>Ошибка</th>
            <th>Причина</th>
        </tr>
        """.trimIndent()


val featuresTableHeader = """
    <tr>
        <th><p>Задача</p></th>
        <th><p>Приоритет</p></th>
        <th><p>Дата создания</p></th>
        <th><p>Дата изменения</p></th>
    </tr>
""".trimIndent()
