package com.sgzmd.flibustier.web.scheduled

interface UserNotifier {
  fun notifyUser(userId: String, updated: String)
}
