package com.sgzmd.flibustier.web.db.entity

import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.security.UserIdProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Transient

@Entity
class TrackedEntry(val entryType: FoundEntryType?, val entryName: String?, val entryId: Int?, val numEntries: Int = 0) {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    val userId = 1

    constructor() : this(null, null, null)

    override fun toString(): String {
        return "TrackedEntry(type=$entryType, name=$entryName, entryId=$entryId, id=$id for user=$userId)"
    }
}