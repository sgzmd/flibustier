package com.sgzmd.flibustier.web.db

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GlobalSearch : IGlobalSearch {
    @Autowired private lateinit var connectionProvider: ConnectionProvider
    private val logger = LoggerFactory.getLogger(GlobalSearch::class.java)

    data class SearchResult(val entryType: FoundEntryType, val name: String, val author: String, val entryId: Int, val numEntities: Int)

    override fun search(searchTerm: String) : List<SearchResult> {
        logger.info("Searching for '$searchTerm'")

        var q = rewriteQuery(searchTerm)

        val statement = connectionProvider.connection?.createStatement()
        val prs = connectionProvider.connection?.prepareStatement("select\n" +
            "    f.SeqName,\n" +
            "    f.Authors,\n" +
            "    f.SeqId,\n" +
            "    (select count(ls.BookId) from libseq ls where ls.SeqId = f.SeqId) NumBooks\n" +
            "from sequence_fts f where f.sequence_fts match ?")
        prs?.setString(1, searchTerm + "*")
        val rs = prs?.executeQuery()

        val sequences = mutableListOf<SearchResult>()
        if (rs == null) {
            return emptyList()
        }

        while (rs.next()) {
            sequences.add(SearchResult(
                FoundEntryType.SERIES,
                rs.getString("SeqName"),
                rs.getString("Authors"),
                rs.getInt("SeqId"),
                rs.getInt("NumBooks")))
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

@Component
interface IGlobalSearch {
    fun search(searchTerm: String) : List<GlobalSearch.SearchResult>
}
