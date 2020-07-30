package com.sgzmd.flibustier.web.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component

@Component
class EmailConfig(
    @Value("\${smtp.username}") val username: String,
    @Value("\${smtp.password}") val password: String,
    @Value("\${smtp.host}") val host: String) {

  val log = LoggerFactory.getLogger(EmailConfig::class.java)

  @Bean
  fun getEmailSender() : JavaMailSender {
    log.info("Creating EmailSender")

    val mailSender = JavaMailSenderImpl()
    mailSender.host = host
    mailSender.port = 25
    mailSender.username = username
    mailSender.password = password

    val props = mailSender.javaMailProperties
    props.put("mail.transport.protocol", "smtp")
    props.put("mail.smtp.auth", "true")
    props.put("mail.debug", "true")

    return mailSender
  }

}