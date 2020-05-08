package com.sgzmd.flibustier.web.db.entity

import com.sgzmd.flibustier.web.db.FoundEntryType
import javax.persistence.*

@Entity
@Table(name = "books")
class Book(val bookName: String?, val bookId: Int?) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var bookEntityId: Long = 0

    @ManyToMany(mappedBy = "books")
    var trackedEntries: List<TrackedEntry> = mutableListOf()
}

@Entity
class TrackedEntry(
    val entryType: FoundEntryType?,
    val entryName: String?,
    val entryId: Int?,
    var numEntries: Int = 0,
    val userId: String = "") {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "TrackedEntryBooks",
    joinColumns = [JoinColumn(name = "id", referencedColumnName = "id")],
    inverseJoinColumns = [JoinColumn(name = "bookEntityId", referencedColumnName = "bookEntityId")])
    var books: List<Book> = mutableListOf()

    // I cannot make enum comparison work with Thymeleaf so this is a workaround until then.
    fun isSeries(): Boolean = entryType == FoundEntryType.SERIES
    fun isAuthor(): Boolean = entryType == FoundEntryType.AUTHOR

    constructor() : this(null, null, null)

    override fun toString(): String {
        return "TrackedEntry(type=$entryType, name=$entryName, entryId=$entryId, id=$id for user=$userId)"
    }
}
