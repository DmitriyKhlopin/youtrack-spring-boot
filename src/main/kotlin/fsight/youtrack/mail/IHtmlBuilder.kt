package fsight.youtrack.mail

interface IHtmlBuilder {
    fun buildMessage(body: String): String
}
