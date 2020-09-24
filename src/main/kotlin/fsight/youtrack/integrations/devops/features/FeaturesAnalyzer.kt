package fsight.youtrack.integrations.devops.features

import fsight.youtrack.CC
import fsight.youtrack.TEST_MAIL_RECEIVER
import fsight.youtrack.db.IDevOpsProvider
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.db.models.devops.toTableRow
import fsight.youtrack.mail.IHtmlBuilder
import fsight.youtrack.mail.IMailSender
import fsight.youtrack.splitToList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FeaturesAnalyzer : IFeaturesAnalyzer {
    @Autowired
    private lateinit var ms: IDevOpsProvider

    @Autowired
    private lateinit var mailSender: IMailSender

    @Autowired
    private lateinit var htmlBuilder: IHtmlBuilder

    @Autowired
    lateinit var pg: IPGProvider

    override fun analyze(): Any {
        analyzePendingFeatures()
        analyzeRejectedFeatures()
        return ""
    }

    override fun analyzePendingFeatures(): Any {
        val i = ms.getFeaturesByPlanningBoardStates(listOf("На рассмотрении РО"))
        val fieldIds = listOf(10320)
        val columns = listOf("Id", "FieldId", "IntValue", "FloatValue", "DateTimeValue", "StringValue")
        val assignees = pg.getDevOpsAssignees()
        val j = ms.getCustomFieldsValues(i.map { it.id }, fieldIds, columns)
        i.forEach { f -> f.project = j.firstOrNull { e -> e.id == f.id && e.fieldId == 10320 }?.string ?: "" }
        i.groupBy { it.assignee }
            .map {
                it.key to it.value
                    .sortedBy { j -> j.priority }
                    .mapIndexed { index, devOpsFeature ->
                        devOpsFeature.ord = index + 1
                        devOpsFeature
                    }
                    .joinToString("") { i -> i.toTableRow() }
            }
            .forEach { m ->
                mailSender.sendHtmlMessage(
                    assignees.firstOrNull { a -> a.fullName.contains(m.first) }?.email ?: TEST_MAIL_RECEIVER,
                    CC.splitToList(delimiters = ",").toTypedArray(),
                    "Оценка внешних запросов на доработку",
                    htmlBuilder.createFeaturesMessage(m.first, m.second)
                )
            }
        return ""
    }

    override fun analyzeRejectedFeatures(): Any {
        val i = ms.getFeaturesByPlanningBoardStates(listOf("Отклонено", "На уточнении"))
        val fieldIds = listOf(10320)
        val columns = listOf("Id", "FieldId", "IntValue", "FloatValue", "DateTimeValue", "StringValue")
        val assignees = pg.getDevOpsAssignees()
        val j = ms.getCustomFieldsValues(i.map { it.id }, fieldIds, columns)
        /*j.forEach { println(it) }
        i.forEach { f -> f.project = j.firstOrNull { e -> e.id == f.id && e.fieldId == 10320 }?.string ?: "" }
        i.groupBy { it.assignee }
            .map { it.key to it.value.sortedBy { j -> j.priority }.joinToString("") { i -> i.toTableRow() } }
            .forEach { m ->
                mailSender.sendHtmlMessage(
                    assignees.firstOrNull { a -> a.fullName.contains(m.first) }?.email ?: TEST_MAIL_RECEIVER,
                    CC.splitToList(delimiters = ",").toTypedArray(),
                    "Оценка внешних запросов на доработку",
                    htmlBuilder.createFeaturesMessage(m.first, m.second)
                )
            }*/
        return ""
    }
}

