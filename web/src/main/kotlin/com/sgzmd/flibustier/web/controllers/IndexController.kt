package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.IGlobalSearch
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.security.AuthenticationFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class IndexController(
    @Autowired val globalSearch: IGlobalSearch,
    @Autowired val trackedEntryRepo: TrackedEntryRepository,
    @Autowired val authFacade: AuthenticationFacade) {

    @GetMapping("/")
    fun index(
            @RequestParam(name = "search_term", required = false) searchTerm: String?,
            model: Model) : String {
        model.addAttribute("name", "world")
        model.addAttribute("username", authFacade.getUserId())

        if (searchTerm != null && searchTerm.length > 1) {
            val results = globalSearch.search(searchTerm!!)
            // empty results should be handled gracefully with an alert.
            model.addAttribute("searchResults", results)
        }

        val userId = authFacade.getUserId()
        val trackedEntries = trackedEntryRepo.findByUserId(userId)
        model.addAttribute("tracked", trackedEntries)

        return "index"
    }
}