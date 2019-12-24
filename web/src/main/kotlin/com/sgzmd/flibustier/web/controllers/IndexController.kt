package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.GlobalSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class IndexController() {
    @Autowired lateinit var globalSearch: GlobalSearch

    @GetMapping("/")
    fun index(
            @RequestParam(name = "search_term", required = false) searchTerm: String?,
            model: Model) : String {
        model.addAttribute("name", "world")

        if (searchTerm != null) {
            val results = globalSearch.search(searchTerm!!)
            if (!results.isEmpty()) {
                model.addAttribute("searchResults", results)
            }
        }

        return "index"
    }
}