package com.sgzmd.flibustier.web.scheduled

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.Thymeleaf

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
    msg.setFrom("flibustier@kro.r-k.co")
    mailSender.send(msg)

    auditLog.info("Message $msg sent.")
  }

  override fun notifyUser(update: Update?) {
    // This is safe - the only reason we use nullable update is for Mockito
    val upd = update!!

    var updateText = "<html><body>"
    for (entry in update.entries.keys) {
      val newBooks = update.entries[entry]!!
      updateText += "<h1>" + entry.entryType.toString() + ": " + entry.entryName + "</h1>"
      updateText += "<ul>"
      for (book in newBooks) {
        updateText += "<li><a href='http://flibusta.is/b/" + book.bookId + "'>" + book.bookName + "</a></li>"
      }
      updateText += "</ul>"
    }

    updateText += "</body></html>"

    sendMessage(upd.userId, updateText)
  }

  fun sendMessage(userId: String, updateText: String) {
    val mimeMessage = mailSender.createMimeMessage()
    val helper = MimeMessageHelper(mimeMessage, "utf-8")
    helper.setText(updateText)
    helper.setTo(userId)
    helper.setFrom("flibustier@kro.r-k.co")
    helper.setSubject("Updates found by Flibustier")

    mailSender.send(mimeMessage)

    // TODO: remove me once verified
    notifyUser(userId, updateText)
  }
}

@Component
@Profile("dev")
class FakeUserNotifier : UserNotifier {
  override fun notifyUser(userId: String, updated: String) {

  }

  override fun notifyUser(update: Update?) {
    TODO("Not yet implemented")
  }
}