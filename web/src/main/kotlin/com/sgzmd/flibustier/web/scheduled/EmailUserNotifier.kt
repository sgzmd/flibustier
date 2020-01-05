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
class EmailUserNotifier(@Autowired val mailSender: JavaMailSender,
                        @Value("\${flibustier.myemail}") val myemail: String) : UserNotifier {
  val logger = LoggerFactory.getLogger(EmailUserNotifier::class.java)
  override fun notifyUser(userId: String, updated: String) {
    logger.info("Notifying user $userId with update $updated")
    // Once we have proper users, this will have to be rewritten.
    if (userId == "sgzmd") {
      val msg = SimpleMailMessage()
      msg.setTo(myemail)
      msg.setSubject("Updates detected by Flibustier")
      msg.setText(updated)
      mailSender.send(msg)

      logger.info("Message $msg sent.")
    }
  }
}