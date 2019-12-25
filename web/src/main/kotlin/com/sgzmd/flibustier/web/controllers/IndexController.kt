package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.GlobalSearch
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.security.AuthenticationFacade
import com.sgzmd.flibustier.web.security.UserIdProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class IndexController() {
    @Autowired lateinit var globalSearch: GlobalSearch
    @Autowired lateinit var trackedEntryRepo: TrackedEntryRepository
    @Autowired lateinit var authFacade: AuthenticationFacade


    @GetMapping("/")
    fun index(
            @RequestParam(name = "search_term", required = false) searchTerm: String?,
            model: Model) : String {
        model.addAttribute("name", "world")

        if (searchTerm != null && searchTerm.length > 1) {
            val results = globalSearch.search(searchTerm!!)
            if (!results.isEmpty()) {
                model.addAttribute("searchResults", results)
            }
        }

        val userId = authFacade.getUserId()
        val trackedEntries = trackedEntryRepo.findByUserId(userId)
        model.addAttribute("tracked", trackedEntries)

        return "index"
    }
}