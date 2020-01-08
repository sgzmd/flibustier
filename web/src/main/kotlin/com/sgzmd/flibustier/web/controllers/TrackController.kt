package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
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
                    SELECT lsn.SeqName, ls.SeqId, COUNT(1) NumEntries 
                    FROM libseq ls, libseqname lsn 
                    WHERE ls.seqId = lsn.seqId and ls.seqId = ?
                    GROUP BY ls.seqId
                """.trimIndent()
        val prs = connectionProvider.connection?.prepareStatement(sql)
        prs?.setInt(1, entryId)
        val rs = prs?.executeQuery()
        if (rs != null) {
          if (rs.next()) {
            val seqName = rs.getString("SeqName")
            val numEntries = rs.getInt("NumEntries")

            repo.save(TrackedEntry(FoundEntryType.SERIES, seqName, entryId, numEntries, user))
          }
        }
      }
      FoundEntryType.AUTHOR -> {
        val sql = """
                    select 
                      count(1) NumEntries, 
                      a.authorName
                    from libbook lb, libavtor la, author_fts a
                    where la.BookId = lb.BookId 
                    and a.authorId = la.AvtorId
                    and lb.Deleted != '1'
                    and la.AvtorId = ?
                    group by la.AvtorId;
                """.trimIndent()
        val prs = connectionProvider.connection?.prepareStatement(sql)
        prs?.setInt(1, entryId)
        val rs = prs?.executeQuery()
        if (rs != null) {
          if (rs.next()) {
            val authorName = rs.getString("authorName")
            val numEntries = rs.getInt("NumEntries")

            repo.save(TrackedEntry(FoundEntryType.AUTHOR, authorName, entryId, numEntries, user))
          }
        }

      }
      else -> {
        logger.warn("Entry type $entryType is not supported yet")
      }
    }

    return RedirectView("/")
  }
}