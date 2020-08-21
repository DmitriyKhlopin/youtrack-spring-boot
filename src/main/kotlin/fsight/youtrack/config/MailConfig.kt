package fsight.youtrack.config

import fsight.youtrack.MAIL_SERVER_ADDRESS
import fsight.youtrack.MAIL_SERVER_PORT
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig {

    @Bean
    fun getJavaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = MAIL_SERVER_ADDRESS
        mailSender.port = MAIL_SERVER_PORT
        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "false"
        props["mail.smtp.starttls.enable"] = "false"
        props["mail.debug"] = "false"
        return mailSender
    }

}