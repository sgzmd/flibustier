package com.sgzmd.flibustier.web.scheduled

import com.sgzmd.flibustier.web.db.IEntryUpdateStatusProvider
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UpdateChecker(
    @Autowired val trackedEntryRepository: TrackedEntryRepository,
    @Autowired val entryUpdateStatusProvider: IEntryUpdateStatusProvider,
    @Autowired val updateNotifier: UserNotifier) {

  fun checkUpdates() {
    val trackedEntries = trackedEntryRepository.findAll().toList()
    val requiredUpdates = entryUpdateStatusProvider.checkForUpdates(trackedEntries)

    val updatesByUser = requiredUpdates.groupBy { it.entry.userId }
    for (userId in updatesByUser.keys) {
      val updates = updatesByUser[userId]
      val updateText = updates?.map { it.entry.entryName }?.joinToString()
      updateNotifier.notifyUser(userId, updateText!!)
    }
  }
}
