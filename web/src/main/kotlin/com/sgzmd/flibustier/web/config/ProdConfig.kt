package com.sgzmd.flibustier.web.config

import com.sgzmd.flibustier.web.scheduled.UpdateChecker
import com.sgzmd.flibustier.web.scheduled.UserNotifier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("prod")
@EnableScheduling
class ProdConfig {
  @Autowired
  lateinit var updateChecker: UpdateChecker

  @Bean
  fun getUserNotifier() = object : UserNotifier {
    val logger = LoggerFactory.getLogger(DevConfig::class.java)
    override fun notifyUser(userId: String, updated: String) {
      logger.info("PRODUCTION NotifyUser: $userId, update: $updated")
    }
  }

  @Scheduled(cron = "5 4 * * * *")
  fun checkUpdates() {
    updateChecker.checkUpdates()
  }
}