package fsight.youtrack.mail

interface IMailSender {
    fun sendMail(from: String,to: String,  subject: String, text: String)
    fun sendHtmlMessage(to: Array<String>, cc: Array<String>?, subject: String, text: String)
}
