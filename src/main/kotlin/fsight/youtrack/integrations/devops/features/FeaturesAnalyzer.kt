package fsight.youtrack.integrations.devops.features

import fsight.youtrack.TEST_MAIL_RECEIVER
import fsight.youtrack.db.IMSProvider
import fsight.youtrack.db.models.toTableRow
import fsight.youtrack.mail.IHtmlBuilder
import fsight.youtrack.mail.IMailSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FeaturesAnalyzer() : IFeaturesAnalyzer {
    @Autowired
    private lateinit var ms: IMSProvider

    @Autowired
    private lateinit var mailSender: IMailSender

    @Autowired
    private lateinit var htmlBuilder: IHtmlBuilder

    override fun analyze(): Any {
        ms.getPendingFeatures()
            .groupBy { it.assignee }
            .map { it.key to it.value.sortedBy { j -> j.priority }.joinToString("") { i -> i.toTableRow() } }
            .forEach { mailSender.sendHtmlMessage(TEST_MAIL_RECEIVER, "Features", htmlBuilder.createFeaturesMessage(it.first, it.second)) }
        return ""
        /*.map { it.key to it.value.map { i -> i.toTableRow() } }*/
        /*.forEach { (t, u) -> mailSender.sendHtmlMessage(TEST_MAIL_RECEIVER, "Features", t.first().toString()) }*/
    }
}

