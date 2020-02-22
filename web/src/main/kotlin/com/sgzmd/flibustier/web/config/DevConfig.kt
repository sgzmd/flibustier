package com.sgzmd.flibustier.web.config

import com.sgzmd.flibustier.web.scheduled.EmailUserNotifier
import com.sgzmd.flibustier.web.scheduled.UpdateChecker
import com.sgzmd.flibustier.web.scheduled.UserNotifier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("dev")
@EnableScheduling
class DevConfig {
  @Autowired lateinit var updateChecker: UpdateChecker
  val log = LoggerFactory.getLogger(DevConfig::class.java)

  @Scheduled(fixedDelay = 5000)
  fun checkUpdates() {
    updateChecker.checkUpdates()
  }
}