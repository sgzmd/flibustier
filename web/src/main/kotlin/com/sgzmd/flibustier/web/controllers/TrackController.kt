package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView

@Controller
class TrackController(val repo: TrackedEntryRepository) {
    private val logger = LoggerFactory.getLogger(TrackController::class.java)

    @GetMapping("/track")
    fun track(@RequestParam(name = "entryId", required = true) entryId: Int,
              @RequestParam(name = "entryName", required = true) entryName: String,
              @RequestParam(name = "entryType", required = true) entryType: FoundEntryType) : RedirectView {
        logger.info("Tracking $entryName ($entryId) of type $entryType")
        repo.save(TrackedEntry(entryType, entryName, entryId))

        return RedirectView("/")
    }
}