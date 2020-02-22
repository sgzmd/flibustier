package com.sgzmd.flibustier.web.scheduled

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
@Profile("prod")
class EmailUserNotifier(@Autowired val mailSender: JavaMailSender) : UserNotifier {
    val auditLog = LoggerFactory.getLogger("audit")

    override fun notifyUser(userId: String, updated: String) {
        auditLog.info("Notifying user $userId with update $updated")
        // Once we have proper users, this will have to be rewritten.

        val msg = SimpleMailMessage()
        msg.setTo(userId)
        msg.setSubject("Updates detected by Flibustier")
        msg.setText(updated)
        mailSender.send(msg)

        auditLog.info("Message $msg sent.")
    }
}

@Component
@Profile("dev")
class FakeUserNotifier() : UserNotifier {
    override fun notifyUser(userId: String, updated: String) {

    }
}