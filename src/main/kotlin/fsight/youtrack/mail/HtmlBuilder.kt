package fsight.youtrack.mail

import fsight.youtrack.errorsTableHeader
import fsight.youtrack.htmlHead
import fsight.youtrack.integrations.devops.revisions.DevOpsRevisions
import org.springframework.stereotype.Service

@Service
class HtmlBuilder : IHtmlBuilder {
    override fun buildMessage(body: String): String {
        return """
            <html>
                    $htmlHead
                        <body>
                            $body                                           
                        </body>
                    </html>   
        """.trimIndent().replace("[\n\r]".toRegex(), "").replace("\\s+".toRegex(), " ")
    }
} 
