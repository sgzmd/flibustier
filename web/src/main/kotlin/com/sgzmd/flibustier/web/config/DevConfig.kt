package com.sgzmd.flibustier.web.config

import com.sgzmd.flibustier.web.scheduled.UserNotifier
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class DevConfig {
  @Bean
  fun getUserNotifier() = object : UserNotifier {
    val logger = LoggerFactory.getLogger(DevConfig::class.java)
    override fun notifyUser(userId: String, updated: String) {
      logger.info("NotifyUser: $userId, update: $updated")
    }
  }
}