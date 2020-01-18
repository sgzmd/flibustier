package com.sgzmd.flibustier.web.db.entity

import com.sgzmd.flibustier.web.db.FoundEntryType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class TrackedEntry(
    val entryType: FoundEntryType?,
    val entryName: String?,
    val entryId: Int?,
    val numEntries: Int = 0,
    val userId: String = "") {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    // I cannot make enum comparison work with Thymeleaf so this is a workaround until then.
    fun isSeries(): Boolean = entryType == FoundEntryType.SERIES
    fun isAuthor(): Boolean = entryType == FoundEntryType.AUTHOR

    constructor() : this(null, null, null)

    override fun toString(): String {
        return "TrackedEntry(type=$entryType, name=$entryName, entryId=$entryId, id=$id for user=$userId)"
    }
}