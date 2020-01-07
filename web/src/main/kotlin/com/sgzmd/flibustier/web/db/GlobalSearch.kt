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

        val series = getSeriesResults(searchTerm)
        val authors = getAuthorResults(searchTerm)

        val result = mutableListOf<SearchResult>()
        result.addAll(authors)
        result.addAll(series)

        return result
    }

    internal fun getAuthorResults(searchTerm: String) : List<SearchResult> {
        val authors = mutableListOf<SearchResult>()
        val statement = connectionProvider.connection?.createStatement()
        val prs = connectionProvider.connection?.prepareStatement("""
            select 
                a.authorName, 
                a.authorId,
                COUNT(1) as Count
            from 
                author_fts a,
                libavtor la,
                libbook lb
            where     
                a.author_fts match(?)
                and la.AvtorId = a.authorId
                and la.BookId = lb.BookId
                and lb.Deleted != '1'
            GROUP BY 1,2;
            ;""".trimIndent())
        prs?.setString(1, searchTerm + "*")
        val rs = prs?.executeQuery()

        val sequences = mutableListOf<SearchResult>()
        if (rs == null) {
            return emptyList()
        }

        while (rs.next()) {
            authors.add(SearchResult(
                    entryType = FoundEntryType.AUTHOR,
                    name = rs.getString("authorName"),
                    author = rs.getString("authorName"),
                    entryId = rs.getInt("authorId"),
                    numEntities = rs.getInt("Count")))
        }

        return authors
    }

    internal fun getSeriesResults(searchTerm: String): List<SearchResult> {
        var q = rewriteQuery(searchTerm)

        val statement = connectionProvider.connection?.createStatement()
        val prs = connectionProvider.connection?.prepareStatement("""select
        f.SeqName,
        f.Authors,
        f.SeqId,
        (select count(ls.BookId) from libseq ls where ls.SeqId = f.SeqId) NumBooks
    from sequence_fts f where f.sequence_fts match ?""")
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
