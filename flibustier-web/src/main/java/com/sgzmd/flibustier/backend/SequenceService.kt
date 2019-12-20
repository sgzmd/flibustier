package com.sgzmd.flibustier.backend

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.ResultSet
import java.util.logging.Logger
import java.util.stream.Stream

@Component
class SequenceService {
    @Autowired
    lateinit var connection: Connection
    var count: Int? = null

    val logger = Logger.getLogger(SequenceService::javaClass.name)

    fun fetch(query: String, offset: Int, limit: Int) : Stream<Series> {
        logger.info("Fetching for q=$query with offset=$offset, limit=$limit")

        var q = rewriteQuery(query)
        var rs: ResultSet? = null

        val statement = connection.createStatement()
        if (query == "") {
            rs = statement.executeQuery("select SeqName, Authors, SeqId from sequence_fts LIMIT $limit OFFSET $offset")
        } else {
            rs = statement.executeQuery("select SeqName, Authors, SeqId from sequence_fts " +
                    "where sequence_fts match '$q' LIMIT $limit OFFSET $offset")
        }

        val sequences = mutableListOf<Series>()
        while (rs.next()) {
            sequences.add(Series(
            rs.getString("SeqName"),
            rs.getString("Authors"),
            rs.getInt("SeqId")))
        }

        return sequences.stream()
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


    fun numEntries(query: String) : Int {
        var rs: ResultSet? = null
        val statement = connection.createStatement()
        if (query == "") {
            rs = statement.executeQuery("select COUNT(1) as Cnt from sequence_fts")
        } else {
            var q = rewriteQuery(query)
            rs = statement.executeQuery("select COUNT(1) as Cnt from sequence_fts " +
                    "where sequence_fts match '$q'")
        }

        if (rs.next()) {
            return rs.getInt("Cnt")
        } else {
            return 0;
        }
    }
}