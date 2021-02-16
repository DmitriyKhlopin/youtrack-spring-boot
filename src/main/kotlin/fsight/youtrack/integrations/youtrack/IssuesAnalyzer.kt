package fsight.youtrack.integrations.youtrack

import fsight.youtrack.api.issues.toTableRow
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.mail.IHtmlBuilder
import fsight.youtrack.mail.IMailSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime


@Service
class IssuesAnalyzer : IIssuesAnalyzer {

    @Autowired
    private lateinit var mailSender: IMailSender

    @Autowired
    private lateinit var htmlBuilder: IHtmlBuilder

    @Autowired
    lateinit var pg: IPGProvider

    override fun analyze(): Any {
        val items = pg.getUnresolvedIssuesWithLastCommentByUser()
            .filter { Duration.between(it.created?.toLocalDateTime(), LocalDateTime.now()).toDays() > 1 }
            .mapIndexed { index, issueWiThDetails -> issueWiThDetails.toTableRow(index + 1) }
            .joinToString(separator = "")
        val html = htmlBuilder.createUnresolvedIssuesMessage(items)
        val firstLineAgents = pg.getFirstLineEmployees().map { it.email }
        val firstLineLeadAgents = pg.getFirstLineLeadEmployees().map { it.email }
        mailSender.sendHtmlMessage(firstLineAgents.toTypedArray(), firstLineLeadAgents.toTypedArray(), "Задачи, ожидающие обратной связи", html)
        return html
    }
}
