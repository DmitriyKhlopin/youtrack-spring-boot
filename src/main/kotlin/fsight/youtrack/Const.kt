package fsight.youtrack

import org.springframework.http.HttpHeaders

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


const val SINGLE_ENUM_ISSUE_CUSTOM_FIELD =
    "jetbrains.charisma.customfields.complex.enumeration.SingleEnumIssueCustomField"
const val SINGLE_USER_ISSUE_CUSTOM_FIELD = "jetbrains.charisma.customfields.complex.user.SingleUserIssueCustomField"
const val STATE_ISSUE_CUSTOM_FIELD = "jetbrains.charisma.customfields.complex.state.StateIssueCustomField"
const val SIMPLE_ISSUE_CUSTOM_FIELD = "jetbrains.charisma.customfields.simple.common.SimpleIssueCustomField"
const val SINGLE_VERSION_ISSUE_CUSTOM_FIELD =
    "jetbrains.charisma.customfields.complex.version.SingleVersionIssueCustomField"
const val SINGLE_BUILD_ISSUE_CUSTOM_FIELD =
    "jetbrains.charisma.customfields.complex.build.SingleBuildIssueCustomField"
const val DATE_ISSUE_CUSTOM_FIELD = "jetbrains.charisma.customfields.simple.integer.DateIssueCustomField"
const val TEXT_ISSUE_CUSTOM_FIELD = "jetbrains.charisma.customfields.simple.text.api.TextIssueCustomField"
const val PERIOD_ISSUE_CUSTOM_FIELD = "jetbrains.youtrack.timetracking.periodField.PeriodIssueCustomField"
const val STATE_MACHINE_ISSUE_CUSTOM_FIELD = "jetbrains.charisma.workflow.statemachine.StateMachineIssueCustomField"
const val MULTI_ENUM_ISSUE_CUSTOM_FIELD =
    "jetbrains.charisma.customfields.complex.enumeration.MultiEnumIssueCustomField"
const val SINGLE_OWNED_ISSUE_CUSTOM_FIELD =
    "jetbrains.charisma.customfields.complex.ownedField.SingleOwnedIssueCustomField"
const val MULTI_VERSION_ISSUE_CUSTOM_FIELD =
    "jetbrains.charisma.customfields.complex.version.MultiVersionIssueCustomField"
