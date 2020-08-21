package fsight.youtrack.api.mail

import fsight.youtrack.DEFAULT_MAIL_SENDER
import fsight.youtrack.TEST_MAIL_RECEIVER
import fsight.youtrack.mail.IMailSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class MailController {
    @Autowired
    lateinit var emailSender: IMailSender

    @GetMapping("/api/mail")
    fun sendMail(): String {
        emailSender.sendMail(from = DEFAULT_MAIL_SENDER, to = TEST_MAIL_RECEIVER, subject = "Test Simple Email", text = "Hello, Im testing Simple Email");
        return "Email sent"
    }
}