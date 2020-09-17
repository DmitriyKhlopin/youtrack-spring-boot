package fsight.youtrack.integrations.devops.revisions

import fsight.youtrack.*
import fsight.youtrack.generated.jooq.tables.CustomFieldValues.CUSTOM_FIELD_VALUES
import fsight.youtrack.generated.jooq.tables.IssueTags.ISSUE_TAGS
import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import fsight.youtrack.mail.IMailSender
import fsight.youtrack.models.DevOpsWorkItem
import org.jetbrains.exposed.sql.Database
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class DevOpsRevisions(private val dsl: DSLContext, @Qualifier("tfsDataSource") private val ms: Database) : IDevOpsRevisions {
    @Autowired
    lateinit var mailSender: IMailSender

    override fun startRevision() {
        mailSender.sendMail(DEFAULT_MAIL_SENDER, TEST_MAIL_RECEIVER, "Запущена проверка багов", "Выполняется поиска багов, не взятых в спринт.")
        getActiveBugsAndFeatures(9, null, null)
        mailSender.sendMail(DEFAULT_MAIL_SENDER, TEST_MAIL_RECEIVER, "Проверка багов завершена", "Проверка багов завершена")
    }

    override fun checkBugs(): List<Int> {
        val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (1) and TeamProjectCollectionSK = 37"""
        val all = statement.execAndMap(ms) { ExposedTransformations().toJsonObject(it, listOf("System_Id", "System_State", "IterationPath")) }
        val filtered = all.filter { it["IterationPath"].asString != "Backlog" && it["System_State"].asString != "Closed" && it["System_State"].asString != "Resolved" }
        if (filtered.isEmpty()) all.minBy { it["order"].toString().toInt() } else filtered.minBy { it["order"].toString().toInt() }
        return listOf()
    }

    override fun getActiveBugsAndFeatures(stage: Int?, limit: Int?, offset: Int?): List<Any> {
        val a = getYTWorkItems(limit, offset)
        val b = a.map { it.toT2() }
        if (stage == 1) return b
        val bugs = b.map { it.bugs }.flatten()
        val features = b.map { it.features }.flatten()
        val d = bugs.plus(features).distinct().filter { it < 1000000 }
        if (stage == 2) return d
        val devOpsWorkItems = getDevOpsWorkItems(d)
        b.forEach { t2 -> t2.wiDetails.addAll(devOpsWorkItems.filter { wi -> t2.bugs.contains(wi.systemId) || t2.features.contains(wi.systemId) }) }
        if (stage == 3) return b
        val errors = b.map { it.check() }.filter { it.errors.isNotEmpty() }
        return when (stage) {
            4 -> errors
            5 -> errors.mapNotNull { it.agent }.distinct()
            6 -> errors.groupBy { it.agent }.toList()
            7 -> errors.toT4List()
            8 -> errors.toT4List().map { it.recipient }.distinct()
            9 -> {
                errors.toT4List().forEach { mailSender.sendHtmlMessage(/*TEST_MAIL_RECEIVER*/it.recipient, null, "Автоматическая провека заявок (${it.errors.size})", it.body) }
                errors
            }
            else -> errors
        }
    }

    fun getYTWorkItems(limit: Int?, offset: Int?): MutableList<T1> {
        val agentField: Field<String> = field(name("agents", "agent"), String::class.java)
        val rnField: Field<Int> = field(name("agents", "rn"), Int::class.java)
        val issuesIdField: Field<String> = field(name("agents", "issue_id"), String::class.java)
        val agentsTable = DSL.table(
            """
            (select issue_id,
                           users.email  as agent,
                           row_number() over (partition by issue_id order by wi_created desc ) as rn
                    from work_items
                        left join users on work_items.author_login=users.user_login
                        left join ets_names en on users.email = en.fsight_email
                    where en.support = true)
        """.trimIndent()
        ).asTable().`as`("agents")
        val bugsTable = CUSTOM_FIELD_VALUES.`as`("bugs")
        val typesTable = CUSTOM_FIELD_VALUES.`as`("types")
        val featuresTable = CUSTOM_FIELD_VALUES.`as`("features")
        val statesTable = CUSTOM_FIELD_VALUES.`as`("states")
        val priorityTable = CUSTOM_FIELD_VALUES.`as`("priority")
        val tagsTable = ISSUE_TAGS.`as`("tag")
        val issuesTable = ISSUES.`as`("issues")
        val q = dsl.select(
            issuesTable.ID.`as`("issueId"),
            bugsTable.FIELD_VALUE.`as`("bugs"),
            featuresTable.FIELD_VALUE.`as`("features"),
            priorityTable.FIELD_VALUE.`as`("priority"),
            typesTable.FIELD_VALUE.`as`("type"),
            tagsTable.TAG.`as`("tag"),
            agentField.`as`("agent")
        )
            .from(issuesTable)
            .leftJoin(bugsTable).on(issuesTable.ID.eq(bugsTable.ISSUE_ID).and(bugsTable.FIELD_NAME.eq("Issue")))
            .leftJoin(featuresTable).on(issuesTable.ID.eq(featuresTable.ISSUE_ID).and(featuresTable.FIELD_NAME.eq("Requirement")))
            .leftJoin(statesTable).on(issuesTable.ID.eq(statesTable.ISSUE_ID).and(statesTable.FIELD_NAME.eq("State")))
            .leftJoin(priorityTable).on(issuesTable.ID.eq(priorityTable.ISSUE_ID).and(priorityTable.FIELD_NAME.eq("Priority")))
            .leftJoin(tagsTable).on(issuesTable.ID.eq(tagsTable.ISSUE_ID).and(tagsTable.TAG.eq("Критично")))
            .leftJoin(typesTable).on(issuesTable.ID.eq(typesTable.ISSUE_ID).and(typesTable.FIELD_NAME.eq("Type")))
            .leftJoin(agentsTable).on(issuesTable.ID.eq(issuesIdField).and(rnField.eq(1)))
            .where(statesTable.FIELD_VALUE.eq("Направлена разработчику"))
            .limit(limit ?: Integer.MAX_VALUE)
            .offset(offset ?: 0)
        println(q)
        return q.fetchInto(T1::class.java)
    }

    override fun getDevOpsWorkItems(ids: List<Int>): List<DevOpsWorkItem> {
        val statement =
            "select System_Id, System_State, Microsoft_VSTS_Common_Priority, IterationPath, System_CreatedDate, System_AssignedTo, System_WorkItemType, AreaPath, System_Title, System_CreatedBy from CurrentWorkItemView where System_Id in (${
                ids.joinToString(
                    ","
                )
            })  and TeamProjectCollectionSK = 37 and System_WorkItemType in ('Bug', 'Feature')"
        /*val statement = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${ids.joinToString(",")}) and TeamProjectCollectionSK = 37"""*/
        return statement.execAndMap(ms) { ExposedTransformations().toDevOpsWorkItem(it) }
    }

    fun notify(subject: String, body: String) {
        mailSender.sendHtmlMessage(to = TEST_MAIL_RECEIVER, cc = null, subject = subject, text = body)
    }

    /**
     * Класс для извлечения данных по issue со списком багов и фич из YT
     * */
    data class T1(
        val issueId: String,
        val bugs: String?,
        val features: String?,
        val priority: String?,
        val type: String?,
        val tag: String?,
        val agent: String?
    ) {
        fun toT2(): T2 {
            return T2(
                issueId = this.issueId,
                bugs = this.bugs?.split(",")?.mapNotNull { it.trim().toIntOrNull() }.orEmpty(),
                features = this.features?.split(",")?.mapNotNull { it.trim().toIntOrNull() }.orEmpty(),
                priority = this.priority,
                tag = this.tag,
                type = this.type,
                agent = this.agent
            )
        }
    }

    data class T2(
        val issueId: String,
        val bugs: List<Int>,
        val features: List<Int>,
        val priority: String?,
        val type: String?,
        val tag: String?,
        val wiDetails: MutableList<DevOpsWorkItem> = arrayListOf(),
        val agent: String?
    ) {
        //TODO добавить проверку area
        fun check(): T3 {
            val ytPriority = when {
                this.priority == "Major" && this.tag == "Критично" -> 1
                this.priority == "Major" -> 2
                this.priority == "Normal" -> 3
                this.priority == "Minor" -> 4
                else -> -1
            }
            val devOpsPriority = wiDetails.map { it.priority?.toInt() ?: -1 }.maxBy { it }
            val errors = arrayListOf<CustomError>()
            /**Несоотвествие приоритетов*/
            if (ytPriority != devOpsPriority) {
                val filtered = wiDetails.filter { it.priority != this.priority }
                val reason =
                    filtered.joinToString(separator = "<br />") { "Приоритет в ${it.type} ${getDevOpsUrl(it.systemId)} = ${it.priority}. Приоритет в ${getYouTrackUrl(issueId)} = $ytPriority" }
                errors.add(CustomError(ytId = issueId, reason = "<p>Приоритеты задач не соответствуют</p>", description = "<p>$reason</p>"))
            }
            /**Несоответствие типа - не фича при заполненном номере фичи*/
            if (features.isNotEmpty() && type != "Feature") {
                errors.add(
                    CustomError(
                        ytId = issueId,
                        reason = "<p>Указан неправильный тип задачи</p>",
                        description = "<p>${if (features.size > 1) "Номеа фич" else "Номер фичи"}: ${features.joinToString(separator = ", ") { getDevOpsUrl(it) }}. Тип задачи в ${
                            getYouTrackUrl(
                                issueId
                            )
                        } = $type</p>"
                    )
                )
            }
            return T3(agent = this.agent, source = "YT", errors = errors)
        }
    }

    data class CustomError(
        val ytId: String? = null,
        val reason: String? = null,
        val description: String? = null
    ) {
        fun toTableRow(): String = "<tr><td>${this.reason}</td><td>${this.description}</td></tr>"
    }

    data class T3(
        val agent: String?,
        val source: String,
        val errors: MutableList<CustomError> = arrayListOf()
    ) {


        /*val subject: String
            get() = "В задаче ${this.id} обнаружены ошибки"*/

        val body: String
            get() =
                """<html>
                    $htmlHead
                        <body>
                            <p>Добрый день</p>
                            <p>В задаче <a href="https://support.fsight.ru/issue/${this.source}">${this.source}</a> обнаружены ошибки</p>
                            <table border="1">
                            $errorsTableHeader
                            ${this.errors.joinToString(separator = "") { it.toTableRow() }}
                            </table>
                        </body>
                    </html>            
        """.trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ")

        val content: String
            get() =
                """<div>
                        <p>Задача <a href="https://support.fsight.ru/issue/${this.source}">${this.source}</a></p>
                        <table border="1">
                            $errorsTableHeader
                            ${this.errors.joinToString { it.toTableRow() }}
                        </table>
                   </div>""".trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ")


    }

    fun List<T3>.toT4List(): List<T4> {
        return this.filter { it.agent !in listOf("Елена_Белкина") }.groupBy { it.agent }
            .map {
                T4(
                    recipient = it.key ?: TEST_MAIL_RECEIVER,
                    tableContent = it.value.joinToString(separator = "") { f -> f.errors.joinToString(separator = "") { e -> e.toTableRow() } },
                    errors = it.value.map { e -> e.errors }.flatten().toList()
                )
            }

    }

    data class T4(
        val recipient: String,
        val tableContent: String,
        val errors: List<CustomError>
    ) {
        val body: String
            get() =
                """<html>
                    $htmlHead
                        <body>
                            <p>Добрый день.</p>
                            <p>Пожалуйста, проверьте заполнение полей в заявках.</p>
                            <table border="1">
                            $errorsTableHeader
                            ${this.tableContent}
                            </table>
                        </body>
                    </html>            
        """.trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ")
    }
}


