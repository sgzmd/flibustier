package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.proto.AuthorBooksRequest
import com.sgzmd.flibustier.proto.EntityBookResponse
import com.sgzmd.flibustier.proto.EntityName
import com.sgzmd.flibustier.proto.SequenceBooksRequest
import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.proto.Book as ProtoBook
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import com.sgzmd.flibustier.web.security.AuthenticationFacade
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView

@Controller
class TrackController(val repo: TrackedEntryRepository, val connectionProvider: ConnectionProvider) {
    private val logger = LoggerFactory.getLogger(TrackController::class.java)

    @Autowired
    lateinit var authFacade: AuthenticationFacade

    @GetMapping("/track")
    fun track(
        @RequestParam(name = "entryId", required = true) entryId: Int,
        @RequestParam(name = "entryName", required = true) entryName: String,
        @RequestParam(name = "entryType", required = true) entryType: FoundEntryType
    ): RedirectView {
        logger.info("Tracking $entryName ($entryId) of type $entryType")

        if (!authFacade.authentication().isAuthenticated) {
            logger.error("User is not authenticated")
            return RedirectView("/")
        }

        val auth = authFacade.authentication()
        val user = authFacade.getUserId()

        val allTrackedByUser = repo.findByUserId(user)
        if (allTrackedByUser.filter { it.entryId == entryId && it.entryType == entryType }.isNotEmpty()) {
            // Already tracking this entity
            return RedirectView("/?already_tracking=$entryId&entryType=$entryType")
        }

        var protoBookList: MutableList<ProtoBook>? = null
        var resp: EntityBookResponse? = null
        when (entryType) {
            FoundEntryType.AUTHOR -> {
                val req = AuthorBooksRequest.newBuilder()
                    .setAuthorId(entryId)
                    .build();

                resp = connectionProvider.flibustierServer?.getAuthorBooks(req)
            }
            FoundEntryType.SERIES -> {
                val req = SequenceBooksRequest.newBuilder()
                    .setSequenceId(entryId)
                    .build()

                resp = connectionProvider.flibustierServer?.getSeriesBooks(req)
            }
            else -> {
                logger.warn("Entry type $entryType is not supported yet")
            }
        }

        protoBookList = resp?.bookList
        val bookList = protoBookList?.map { Book(it.bookName, it.bookId) }
        val trackedEntry = TrackedEntry(entryType, resp?.entityName?.readableString(), entryId, bookList!!.size, user)
        trackedEntry.books = bookList!!
        repo.save(trackedEntry)

        return RedirectView("/")
    }

    fun EntityName.readableString() : String {
        return if (this.hasAuthorName()) {
            val authorName = this.authorName
            if (authorName.middleName.isNotEmpty()) {
                String.format("%s %s %s", authorName.firstName, authorName.middleName, authorName.lastName)
            } else {
                String.format("%s %s", authorName.firstName, authorName.lastName)
            }
        } else {
            this.sequenceName
        }
    }
}
