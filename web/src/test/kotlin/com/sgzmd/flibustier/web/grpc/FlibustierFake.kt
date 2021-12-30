package com.sgzmd.flibustier.web.grpc

import com.sgzmd.flibustier.proto.*
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory

class FlibustierFake : FlibustierGrpc.FlibustierImplBase() {

    private val logger = LoggerFactory.getLogger(FlibustierFake::class.java)

    override fun listTrackedEntries(
        request: ListTrackedEntriesRequest?,
        responseObserver: StreamObserver<ListTrackedEntriesResponse>?
    ) {
        val response = ListTrackedEntriesResponse.newBuilder()
            .addEntry(TrackedEntry.newBuilder()
                .setEntryType(EntryType.SERIES)
                .setNumEntries(2)
                .setEntryName("Test")
                .addBook(Book.newBuilder()
                    .setBookId(123)
                    .setBookName("Hello"))
                .addBook(Book.newBuilder()
                    .setBookId(456)
                    .setBookName("World")))
            .build()
        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}