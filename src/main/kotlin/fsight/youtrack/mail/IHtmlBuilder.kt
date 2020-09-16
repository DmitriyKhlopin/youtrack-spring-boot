package fsight.youtrack.mail

interface IHtmlBuilder {
    fun buildMessage(body: String): String
    fun createFeaturesMessage(agent: String, tableBody: String): String
}
