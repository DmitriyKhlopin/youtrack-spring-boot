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
        "iana.maltseva@fsight.ru,viktoriya.zolotaryova@fsight.ru,dmitriy.khlopin@fsight.ru,andrey.zolotaryev@fsight.ru,anton.lykov@fsight.ru,nikolai.parkhachev@fsight.ru"

val gkbQueriesFull = listOf<String>(
        "SELECT u.usename FROM pg_catalog.pg_database d, pg_catalog.pg_user u WHERE d.datname = U&'MAIN_REPOSITORY' and d.datdba = u.usesysid",
        "select now() at time zone 'UTC'",
        "select max(a.VER) as VER from B_LAST a",
        "SELECT tablename as TABLE_NAME, tableowner as OWNER_NAME FROM pg_tables WHERE tablename NOT LIKE 'pg_%' AND tablename NOT LIKE 'sql_%' AND schemaname = current_schema AND upper(tablename) = 'B_ALRT' ORDER BY OWNER_NAME, TABLE_NAME",
        "select nextval('B_LOGSEQ')",
        "select a.STAMP from B_SEC a where a.SEC = 0",
        "select a.BIN, a.BIN_HASH from B_SEC a where a.SEC = 0",
        "select a.DAT, a.ID, a.VI, a.VS from B_SEC_DAT a",
        "select a.STA, a.ID, a.TYP, a.DOM, a.ACC from B_SEC_STA a where a.TYP = 1 and a.DOM = 0",
        "select a.CER_K, a.FLG, a.ID, a.NAM, a.DAT from B_CER_V a",
        "SELECT u.usename FROM pg_catalog.pg_database d, pg_catalog.pg_user u WHERE d.datname = U&'MAIN_REPOSITORY' and d.datdba = u.usesysid",
        "select B_LOGNSUC('repository_manager', 'HLOPIND', 'FS')",
        "select a.OBJ, a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and (a.OBJ in(153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041))",
        "select a.SD, a.BIN from B_SD a where a.HSH = 'YtpYFiMSClwSRuUdqpIgEWaP'",
        "select a.LCL from B_LCL a where a.DFL = 1",
        "select b.SD, b.BIN from B_SD b, (select distinct a.SD from B_OBJ a where (a.DEF = 0)) c where b.SD = c.SD",
        "select a.OBJ, a.CLS, a.ID, a.NAM, a.DES, a.PAR, a.STAMP, a.SD, a.ITL, a.SHC, a.LIN, a.APP, a.DEF, a.TRACK_DEP, a.SPURL, a.MBSOURCE, a.VER, a.ONS from B_OBJ a where (a.DEF = 0)",
        "select distinct a.OBJ from B_CIO a where ((a.SES = 11162) or (a.US_NAM = 'ADMIN' AND a.US_STA = 'HLOPIND'))",
        "select a.STAMP from B_OBJ a where a.OBJ = 44",
        "select a.BIN from B_DAT a where a.OBJ = 44",
        "select b.BIN from B_PAR b where b.OBJ = 44",
        "select a.STAMP from B_OBJ a where a.OBJ = 152",
        "select a.BIN from B_DAT a where a.OBJ = 152",
        "select b.BIN from B_PAR b where b.OBJ = 152",
        "WITH RECURSIVE d(obj) as ( values (153) union select a.REF from B_DEP a, d where a.OBJ = d.obj and a.REF <> d.obj ) select obj from d order by obj ",
        "select a.OBJ, a.BIN from B_PAR a where ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "SELECT a.OBJ, a.STAMP FROM B_OBJ a WHERE ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "SELECT a.OBJ, a.BIN FROM B_DAT a WHERE ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "select a.OBJ, a.LCL, a.NAM, a.DES from B_NAM a where ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        " select a.OBJ from B_DEP a where a.REF = 4118 and a.OBJ <> 4118",
        "select a.OBJ from B_DEP a where a.REF = 4327 and a.OBJ <> 4327",
        "select a.OBJ from B_DEP a where a.REF = 4341 and a.OBJ <> 4341",
        "select a.OBJ, a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "select a.OBJ from B_DEP a where a.REF = 4342 and a.OBJ <> 4342",
        "select a.OBJ from B_DEP a where a.REF = 4343 and a.OBJ <> 4343",
        "select a.OBJ from B_DEP a where a.REF = 4344 and a.OBJ <> 4344",
        "select a.OBJ from B_DEP a where a.REF = 4437 and a.OBJ <> 4437",
        "select a.OBJ from B_DEP a where a.REF = 4436 and a.OBJ <> 4436",
        "select a.OBJ from B_DEP a where a.REF = 4465 and a.OBJ <> 4465",
        "select a.OBJ from B_DEP a where a.REF = 4489 and a.OBJ <> 4489",
        "select a.OBJ from B_DEP a where a.REF = 4876 and a.OBJ <> 4876",
        "select a.STAMP from B_SEC a where a.SEC = 0",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4118",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4327",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4341",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4342",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4343",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4344",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4437",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4436",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4465",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4489",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4876",
        "select a.OBJ, a.LCL, a.NAM, a.DES from B_NAM a where ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "select a.OBJ from B_DEP a where a.REF = 4118 and a.OBJ <> 4118",
        "select a.OBJ from B_DEP a where a.REF = 4327 and a.OBJ <> 4327",
        "select a.OBJ from B_DEP a where a.REF = 4341 and a.OBJ <> 4341",
        "select a.OBJ from B_DEP a where a.REF = 4342 and a.OBJ <> 4342",
        "select a.OBJ from B_DEP a where a.REF = 4342 and a.OBJ <> 4342",
        "select a.OBJ from B_DEP a where a.REF = 4344 and a.OBJ <> 4344",
        "select a.OBJ from B_DEP a where a.REF = 4437 and a.OBJ <> 4437",
        "select a.OBJ from B_DEP a where a.REF = 4436 and a.OBJ <> 4436",
        "select a.OBJ from B_DEP a where a.REF = 4465 and a.OBJ <> 4465",
        "select a.OBJ from B_DEP a where a.REF = 4489 and a.OBJ <> 4489",
        "select a.OBJ from B_DEP a where a.REF = 4876 and a.OBJ <> 4876",
        "select a.STAMP from B_SEC a where a.SEC = 0",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4118",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4327",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4341",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4342",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4343",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4344",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4437",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4436",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4465",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4489",
        "select a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and a.OBJ = 4876"
)

val gkbQueriesShort = listOf<String>(
        "SELECT u.usename FROM pg_catalog.pg_database d, pg_catalog.pg_user u WHERE d.datname = U&'MAIN_REPOSITORY' and d.datdba = u.usesysid",
        "select now() at time zone 'UTC'",
        "select max(a.VER) as VER from B_LAST a",
        "SELECT tablename as TABLE_NAME, tableowner as OWNER_NAME FROM pg_tables WHERE tablename NOT LIKE 'pg_%' AND tablename NOT LIKE 'sql_%' AND schemaname = current_schema AND upper(tablename) = 'B_ALRT' ORDER BY OWNER_NAME, TABLE_NAME",
        "select nextval('B_LOGSEQ')",
        "select a.STAMP from B_SEC a where a.SEC = 0",
        "select a.BIN, a.BIN_HASH from B_SEC a where a.SEC = 0",
        "select a.DAT, a.ID, a.VI, a.VS from B_SEC_DAT a",
        "select a.STA, a.ID, a.TYP, a.DOM, a.ACC from B_SEC_STA a where a.TYP = 1 and a.DOM = 0",
        "select a.CER_K, a.FLG, a.ID, a.NAM, a.DAT from B_CER_V a",
        "SELECT u.usename FROM pg_catalog.pg_database d, pg_catalog.pg_user u WHERE d.datname = U&'MAIN_REPOSITORY' and d.datdba = u.usesysid",
        "select B_LOGNSUC('repository_manager', 'HLOPIND', 'FS')",
        "select a.OBJ, a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and (a.OBJ in(153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041))",
        "select a.SD, a.BIN from B_SD a where a.HSH = 'YtpYFiMSClwSRuUdqpIgEWaP'",
        "select a.LCL from B_LCL a where a.DFL = 1",
        "select b.SD, b.BIN from B_SD b, (select distinct a.SD from B_OBJ a where (a.DEF = 0)) c where b.SD = c.SD",
        "select a.OBJ, a.CLS, a.ID, a.NAM, a.DES, a.PAR, a.STAMP, a.SD, a.ITL, a.SHC, a.LIN, a.APP, a.DEF, a.TRACK_DEP, a.SPURL, a.MBSOURCE, a.VER, a.ONS from B_OBJ a where (a.DEF = 0)",
        "select distinct a.OBJ from B_CIO a where ((a.SES = 11162) or (a.US_NAM = 'ADMIN' AND a.US_STA = 'HLOPIND'))",
        "select a.STAMP from B_OBJ a where a.OBJ = 44",
        "select a.BIN from B_DAT a where a.OBJ = 44",
        "select b.BIN from B_PAR b where b.OBJ = 44",
        "select a.STAMP from B_OBJ a where a.OBJ = 152",
        "select a.BIN from B_DAT a where a.OBJ = 152",
        "select b.BIN from B_PAR b where b.OBJ = 152",
        "WITH RECURSIVE d(obj) as ( values (153) union select a.REF from B_DEP a, d where a.OBJ = d.obj and a.REF <> d.obj ) select obj from d order by obj ",
        "select a.OBJ, a.BIN from B_PAR a where ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "SELECT a.OBJ, a.STAMP FROM B_OBJ a WHERE ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "SELECT a.OBJ, a.BIN FROM B_DAT a WHERE ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "select a.OBJ, a.LCL, a.NAM, a.DES from B_NAM a where ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "select a.OBJ, a.STAMP, a.VERS, a.BIN from B_MOD a where a.MODK = 0 and ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",

        "select a.STAMP from B_SEC a where a.SEC = 0",
        "select a.MODK, a.OBJ, a.STAMP, a.VERS, a.BIN from B_MOD a where (a.MODK = 0 and a.OBJ = 4118) or (a.MODK = 0 and a.OBJ = 4327) or (a.MODK = 0 and a.OBJ = 4341) or (a.MODK = 0 and a.OBJ = 4342) or (a.MODK = 0 and a.OBJ = 4343) or (a.MODK = 0 and a.OBJ = 4344) or (a.MODK = 0 and a.OBJ = 4437) or (a.MODK = 0 and a.OBJ = 4436) or (a.MODK = 0 and a.OBJ = 4465) or (a.MODK = 0 and a.OBJ = 4489) or (a.MODK = 0 and a.OBJ = 4876)",
        "select a.OBJ, a.LCL, a.NAM, a.DES from B_NAM a where ( a.OBJ in (153, 4118, 4119, 4120, 4124, 4126, 4127, 4131, 4134, 4135, 4136, 4138, 4139, 4140, 4153, 4161, 4163, 4165, 4176, 4198, 4200, 4203, 4206, 4207, 4265, 4266, 4267, 4268, 4269, 4271, 4272, 4274, 4284, 4285, 4288, 4289, 4292, 4294, 4295, 4296, 4314, 4326, 4327, 4336, 4341, 4342, 4343, 4344, 4345, 4346, 4368, 4369, 4376, 4377, 4429, 4436, 4437, 4443, 4444, 4445, 4446, 4447, 4448, 4451, 4454, 4461, 4464, 4465, 4474, 4482, 4484, 4489, 4639, 4759, 4780, 4809, 4810, 4870, 4876, 4878, 4887, 4906, 4911, 4944, 4951, 4996, 4998, 4999, 5041) )",
        "select a.ref, a.OBJ from B_DEP a where (a.REF = 4118 and a.OBJ <> 4118) or (a.REF = 4327 and a.OBJ <> 4327)or (a.REF = 4341 and a.OBJ <> 4341) or (a.REF = 4342 and a.OBJ <> 4342) or (a.REF = 4344 and a.OBJ <> 4344) or (a.REF = 4437 and a.OBJ <> 4437) or (a.REF = 4465 and a.OBJ <> 4465) or (a.REF = 4489 and a.OBJ <> 4489) or (a.REF = 4876 and a.OBJ <> 4876)",
        "select a.STAMP from B_SEC a where a.SEC = 0"
)