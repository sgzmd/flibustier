package com.sgzmd.flibustier.web.db

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class GlobalSearch {
    @Autowired private lateinit var connectionProvider: ConnectionProvider
    private val logger = LoggerFactory.getLogger(GlobalSearch::class.java)

    data class SearchResult(val entryType: FoundEntryType, val name: String, val entryId: Int)

    fun search(searchTerm: String) : List<SearchResult> {
        logger.info("Searching for '$searchTerm'")

        var q = rewriteQuery(searchTerm)

        val statement = connectionProvider.connection?.createStatement()
        val rs = statement?.executeQuery("select SeqName, Authors, SeqId from sequence_fts " +
                    "where sequence_fts match '$searchTerm*'")

        val sequences = mutableListOf<SearchResult>()
        if (rs == null) {
            return emptyList()
        }
        while (rs.next()) {
            sequences.add(SearchResult(FoundEntryType.SERIES, rs.getString("SeqName"), rs.getInt("SeqId")))
        }

        return sequences
    }

    private fun rewriteQuery(query: String): String? {
        val re = Regex(".+sequence\\/([0-9]+)")
        var q: String? = query
        if (re.matches(query)) {
            val group = re.matchEntire(query)?.groups?.get(1)
            q = group?.value
        }
        return q
    }
}