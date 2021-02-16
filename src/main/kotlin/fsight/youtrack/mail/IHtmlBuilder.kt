package fsight.youtrack.mail

interface IHtmlBuilder {
    fun buildMessage(head: String, body: String): String
    fun createFeaturesMessage(agent: String, tableBody: String): String
    fun createUnresolvedIssuesMessage(tableBody: String): String
}
