package com.sgzmd.flibustier.web.scheduled

import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry

data class Update(val userId: String, val entries: Map<TrackedEntry, List<Book>>)

interface UserNotifier {
  fun notifyUser(userId: String, updated: String)

  // The only reason we use nullable Update? is that apparently this is the only way
  // to make it work with Mockito's argument captors. This is a KI, Google is suggesting
  // another workaround.
  fun notifyUser(update: Update?)
}
