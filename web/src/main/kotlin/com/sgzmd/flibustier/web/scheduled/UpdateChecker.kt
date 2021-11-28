package com.sgzmd.flibustier.web.scheduled

import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.IEntryUpdateStatusProvider
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class UpdateChecker(
    @Autowired val trackedEntryRepository: TrackedEntryRepository,
    @Autowired val entryUpdateStatusProvider: IEntryUpdateStatusProvider,
    @Autowired val updateNotifier: UserNotifier,
    @Autowired @Value("\${flibusta.dburl}") val dbUrl: String) {

  val logger = LoggerFactory.getLogger(UpdateChecker::class.java)
  val auditLog = LoggerFactory.getLogger("audit")

  fun checkUpdates() {
    logger.info("Starting update checker ...")

    auditLog.info("Starting update checker")
    val trackedEntries = trackedEntryRepository.findAll().toList()

    auditLog.info("There are ${trackedEntries.size} in tracked entries")
    val requiredUpdates = entryUpdateStatusProvider.checkForUpdates(trackedEntries)

    val updatesByUser = requiredUpdates.groupBy { it.entry.userId }
    for (userId in updatesByUser.keys) {
      logger.info("Updating user $userId")
      val updates = updatesByUser[userId]

      if (updates != null) {
        val entriesToBooks = mutableMapOf<TrackedEntry, List<Book>>()
        for (update in updates) {
          if (update.newBooks?.isNotEmpty() == true) {
            entriesToBooks[update.entry] = update.newBooks
          }
        }

        if (entriesToBooks.isNotEmpty()) {
          updateNotifier.notifyUser(Update(userId, entriesToBooks))
        }
      }
    }
  }
}
