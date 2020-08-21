package fsight.youtrack.mail

interface IMailSender {
    fun sendMail(from: String,to: String,  subject: String, text: String)
    fun sendErrorWarning(to: String, subject: String, text: String)
}