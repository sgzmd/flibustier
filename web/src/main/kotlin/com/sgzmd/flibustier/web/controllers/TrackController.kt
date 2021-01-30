package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.Book
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
    fun track(@RequestParam(name = "entryId", required = true) entryId: Int,
              @RequestParam(name = "entryName", required = true) entryName: String,
              @RequestParam(name = "entryType", required = true) entryType: FoundEntryType): RedirectView {
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

        when (entryType) {
            FoundEntryType.SERIES -> {
                val sql = """
                    SELECT lsn.SeqName, ls.SeqId, b.Title, b.BookId
                    FROM libseq ls, libseqname lsn , libbook b
                    WHERE ls.seqId = lsn.seqId and ls.seqId = ? and ls.BookId = b.BookId and b.Deleted != '1'
					          group by b.BookId
					          order by ls.SeqNumb;
                """.trimIndent()
                val prs = connectionProvider.connection?.prepareStatement(sql)
                prs?.setInt(1, entryId)
                val rs = prs?.executeQuery()

                if (rs != null) {
                    var seqName: String? = null
                    var numEntries = 0
                    val books = mutableListOf<Book>()
                    while (rs.next()) {
                        ++numEntries
                        if (seqName == null) {
                            seqName = rs.getString("SeqName")
                        }
                        books.add(Book(rs.getString("Title"), rs.getInt("BookId")))
                    }
                    val trackedEntry = TrackedEntry(FoundEntryType.SERIES, seqName, entryId, numEntries, user)
                    trackedEntry.books = books
                    repo.save(trackedEntry)
                }
            }
            FoundEntryType.AUTHOR -> {
                val sql = """
                    select 
                      a.authorName,
                      lb.Title,
                      lb.Bookid
                    from libbook lb, libavtor la, author_fts a
                    where la.BookId = lb.BookId 
                    and a.authorId = la.AvtorId
                    and lb.Deleted != '1'
                    and la.AvtorId = ?
                    group by la.BookId;
                """.trimIndent()
                val prs = connectionProvider.connection?.prepareStatement(sql)
                prs?.setInt(1, entryId)
                val rs = prs?.executeQuery()
                if (rs != null) {
                    var authorName: String? = null
                    var numEntries = 0
                    val books = mutableListOf<Book>()

                    while (rs.next()) {
                        ++numEntries
                        if (authorName == null) {
                            authorName = rs.getString("authorName")
                        }
                        books.add(Book(rs.getString("Title"), rs.getInt("BookId")))
                    }

                    val trackedEntry = TrackedEntry(FoundEntryType.AUTHOR, authorName, entryId, numEntries, user)
                    trackedEntry.books = books
                    repo.save(trackedEntry)
                }

            }
            else -> {
                logger.warn("Entry type $entryType is not supported yet")
            }
        }

        return RedirectView("/")
    }
}