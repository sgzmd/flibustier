package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.proto.EntryType
import com.sgzmd.flibustier.proto.FlibustierGrpc
import com.sgzmd.flibustier.proto.ListTrackedEntriesRequest
import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.IGlobalSearch
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.security.AuthenticationFacade
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class IndexController(
    @Autowired val globalSearch: IGlobalSearch,
    @Autowired val connectionProvider: ConnectionProvider,
    @Autowired val trackedEntryRepo: TrackedEntryRepository,
    @Autowired val authFacade: AuthenticationFacade) {

    @GrpcClient("flibustier")
    private lateinit var flibustierBlockingStub: FlibustierGrpc.FlibustierBlockingStub

    @GetMapping("/")
    fun index(
            @RequestParam(name = "search_term", required = false) searchTerm: String?,
            model: Model) : String {
        model.addAttribute("name", "world")
        model.addAttribute("username", authFacade.getUserId())
        model.addAttribute("lastUpdated", connectionProvider.getLastUpdateTimestamp())

        if (searchTerm != null && searchTerm.length > 1) {
            val results = globalSearch.search(searchTerm)
            // empty results should be handled gracefully with an alert.
            model.addAttribute("searchResults", results)
        }

        val userId = authFacade.getUserId()
        val trackedEntries = flibustierBlockingStub.listTrackedEntries(ListTrackedEntriesRequest.newBuilder().setUserId(userId).build())

        val pairs = trackedEntries.entryList.groupBy { it.entryType }

        model.addAttribute("authors", pairs[EntryType.AUTHOR]?.sortedBy { it.entryName })
        model.addAttribute("series", pairs[EntryType.SERIES]?.sortedBy { it.entryName })

        model.addAttribute("tracked", trackedEntries)

        return "index"
    }
}