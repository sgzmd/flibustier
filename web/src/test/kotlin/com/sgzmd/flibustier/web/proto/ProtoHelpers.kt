package com.sgzmd.flibustier.web.proto

import com.sgzmd.flibustier.web.db.EntryTypeConverter
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.proto.TrackedEntry as ProtoTrackedEntry
import com.sgzmd.flibustier.web.db.entity.TrackedEntry as DbTrackedEntry


fun ProtoTrackedEntry.toDbEntry(): DbTrackedEntry {

    val entry = DbTrackedEntry(
        EntryTypeConverter.fromProto(this.entryType),
        this.entryName, this.entryId, this.bookCount, this.userId
    )
    entry.books = this.bookList.map { Book(it.bookName, it.bookId) }

    return entry
}

fun DbTrackedEntry.toProtoEntry(): ProtoTrackedEntry = ProtoTrackedEntry.newBuilder()
    .setEntryId(this.entryId!!)
    .setEntryName(this.entryName)
    .setEntryType(EntryTypeConverter.toProto(this.entryType!!))
    .setNumEntries(this.numEntries)
    .addAllBook(this.books.map {
        com.sgzmd.flibustier.proto.Book.newBuilder()
            .setBookId(it.bookId!!)
            .setBookName(it.bookName).build()
    })
    .build()