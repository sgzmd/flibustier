package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.springframework.data.repository.CrudRepository

interface TrackedEntryRepository : CrudRepository<TrackedEntry, Long> {
    fun findByUserId(userId: Int) : List<TrackedEntry>
    fun findByEntryId(entryId: Int) : List<TrackedEntry>
}