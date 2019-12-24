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
class UntrackController(val repo: TrackedEntryRepository) {
    private val logger = LoggerFactory.getLogger(UntrackController::class.java)

    @GetMapping("/untrack")
    fun track(@RequestParam(name = "id", required = true) id: Long) : RedirectView {
        logger.info("Un-tracking Entry $id")

        repo.deleteById(id)

        return RedirectView("/")
    }
}