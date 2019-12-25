package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface TrackedEntryRepository : CrudRepository<TrackedEntry, Long> {
    fun findByUserId(userId: String) : List<TrackedEntry>
    fun findByEntryId(entryId: Int) : List<TrackedEntry>

    @Transactional
    fun deleteByUserIdAndId(userId: String, id: Long) {
        val entity = findById(id)
        if (entity.get().userId == userId) {
            delete(entity.get())
        }
    }
}