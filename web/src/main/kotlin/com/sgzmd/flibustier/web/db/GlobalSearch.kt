package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.proto.FlibustierGrpc
import com.sgzmd.flibustier.proto.SearchRequest
import com.sgzmd.flibustier.proto.SearchResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

// @Component
class GlobalSearch  {
    data class SearchResult(
        val entryType: FoundEntryType,
        val name: String,
        val author: String,
        val entryId: Int,
        val numEntities: Int
    )
}

@Component
class GrpcGlobalSearch : IGlobalSearch {
    @Autowired
    private lateinit var connectionProvider: ConnectionProvider
    private val logger = LoggerFactory.getLogger(GlobalSearch::class.java)

    override fun search(searchTerm: String): List<GlobalSearch.SearchResult> {
        val response = connectionProvider.flibustierServer?.globalSearch(
            SearchRequest.newBuilder()
                .setSearchTerm(searchTerm)
                .build()
        )

        val res = response?.entryList?.map {
            GlobalSearch.SearchResult(
                EntryTypeConverter.fromProto(it.entryType),
                it.entryName,
                it.author,
                it.entryId.toInt(), // safe because less than 4B entries
                it.numEntities
            )
        }

        return res?: emptyList()
    }
}

@Component
interface IGlobalSearch {
    fun search(searchTerm: String): List<GlobalSearch.SearchResult>
}
