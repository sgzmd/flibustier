package com.sgzmd.flibustier.web.config

import com.sgzmd.flibustier.web.scheduled.UpdateChecker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("prod")
@EnableScheduling
class ProdConfig {
  @Autowired
  lateinit var updateChecker: UpdateChecker

  @Scheduled(cron = "5 4 * * * *")
  fun checkUpdates() {
    updateChecker.checkUpdates()
  }
}