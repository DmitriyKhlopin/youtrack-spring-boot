package fsight.youtrack.mail

import fsight.youtrack.DEFAULT_MAIL_SENDER
import fsight.youtrack.MAIL_SERVER_ADDRESS
import fsight.youtrack.MAIL_SERVER_PORT
import mu.KotlinLogging


import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service


@Service
class MailSender : IMailSender {
    private val logger = KotlinLogging.logger {}

    override fun sendMail(from: String, to: String, subject: String, text: String) {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = MAIL_SERVER_ADDRESS
        mailSender.port = MAIL_SERVER_PORT
        val message = SimpleMailMessage()
        message.setTo(to)
        message.setFrom(from)
        message.setSubject(subject)
        message.setText(text)
        mailSender.send(message);
        logger.info { "Отправлено уведомление получателю $to с темой \"$subject\" и текстом \"$text\"" };
    }

    override fun sendHtmlMessage(to: String, subject: String, text: String) {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = MAIL_SERVER_ADDRESS
        mailSender.port = MAIL_SERVER_PORT
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "utf-8")
        helper.setTo(to)
        helper.setFrom(DEFAULT_MAIL_SENDER)
        helper.setSubject(subject)
        val htmlMsg = (text)
        helper.setText("text", htmlMsg)
        mailSender.send(message)
        logger.info { "Отправлено уведомление получателю $to с темой \"$subject\" и текстом \"$text\"" };
    }
}
