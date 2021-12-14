package com.sgzmd.flibustier.web.proto

import com.google.common.truth.Truth.assertThat
import com.sgzmd.flibustier.proto.Book
import com.sgzmd.flibustier.proto.EntryType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.springframework.boot.test.context.SpringBootTest
import com.sgzmd.flibustier.proto.TrackedEntry as ProtoEntry

@SpringBootTest
@RunWith(BlockJUnit4ClassRunner::class)
internal class ProtoHelpersKtTest {
    @Test
    fun testToDbEntry() {
        val protoBooks = listOf(Book.newBuilder().setBookId(1).setBookName("Book 1").build())
        val entry = ProtoEntry.newBuilder()
            .setEntryId(123)
            .setEntryName("Sample")
            .setEntryType(EntryType.SERIES)
            .setNumEntries(1)
            .addAllBook(protoBooks)
            .build()

        val dbEntry = entry.toDbEntry()

        assertThat(dbEntry.isSeries()).isTrue()
        assertThat(dbEntry.entryName).isEqualTo("Sample")
        assertThat(dbEntry.entryId).isEqualTo(123)
        assertThat(dbEntry.numEntries).isEqualTo(1)
        assertThat(dbEntry.books).hasSize(1)
    }

    @Test
    fun testToProtoEntry() {
        val protoBooks = listOf(Book.newBuilder().setBookId(1).setBookName("Book 1").build())
        val entry = ProtoEntry.newBuilder()
            .setEntryId(123)
            .setEntryName("Sample")
            .setEntryType(EntryType.SERIES)
            .setNumEntries(1)
            .addAllBook(protoBooks)
            .build()

        assertThat(entry.toDbEntry().toProtoEntry()).isEqualTo(entry)
    }
}