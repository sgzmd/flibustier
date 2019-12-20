package com.sgzmd.flibustier.backend

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Connection
import java.util.stream.Stream

@Component
class SequenceService {
    @Autowired
    lateinit var connection: Connection
    var count: Int? = null

    fun fetch(query: String, offset: Int, limit: Int) : Stream<Series> {
        val statement = connection.createStatement()
        val res = statement.executeQuery("select SeqName, Authors from sequence_fts where sequence_fts match '$query'")
        val sequences = mutableListOf<Series>()
        while (res.next()) {
            sequences.add(Series(
            res.getString("SeqName"),
            res.getString("Authors")))
        }

        return sequences.stream()
    }

    fun numEntries() : Int {
        if (count != null) {
            return count!!
        }

        synchronized(this) {
            val statement = connection.createStatement()
            val res = statement.executeQuery("select COUNT(1) from SequenceAuthor")
            if (res.next()) {
                count = res.getInt(0)
                return count!!
            }

            return 0
        }
    }
}