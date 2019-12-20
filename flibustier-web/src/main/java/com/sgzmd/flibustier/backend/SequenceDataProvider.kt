package com.sgzmd.flibustier.backend

import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.DataProviderListener
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.shared.Registration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Connection
import java.util.stream.Stream

@Component
open class SequenceDataProvider : DataProvider<Series, String> {
    @Autowired
    lateinit var connection: Connection
    var count: Int? = null

    override fun fetch(query: Query<Series, String>?): Stream<Series> {
        val sequences = executeQuery(query)

        return sequences.stream().limit(query!!.limit?.toLong())
    }

    private fun executeQuery(query: Query<Series, String>?): MutableList<Series> {
        val nothing = mutableListOf<Series>()

        val limit = query?.limit
        if (limit == 0 || query?.filter == null || !query?.filter?.isPresent!!) {
            return nothing
        }
        val q = query?.filter?.get()
        if (q == null || q.length < 3) {
            return nothing
        }

        val statement = connection.createStatement()
        val res = statement.executeQuery("select SeqName, Authors from sequence_fts where sequence_fts match '$q'")
        val sequences = mutableListOf<Series>()
        while (res.next()) {
            sequences.add(Series(
                    res.getString("SeqName"),
                    res.getString("Authors")))
        }

        return sequences
    }

    override fun size(p0: Query<Series, String>?): Int {

        return executeQuery(p0)?.size

        if (count != null) {
            return count!!
        }

        synchronized(this) {
            val statement = connection.createStatement()
            val res = statement.executeQuery("select COUNT(1) from SequenceAuthor")
            if (res.next()) {
                count = res.getInt(1)
                return count!!
            }

            return 0
        }
    }

    override fun addDataProviderListener(p0: DataProviderListener<Series>?): Registration {
        return Registration {  }
    }

    override fun refreshItem(p0: Series?) {

    }

    override fun refreshAll() {
    }

    override fun isInMemory(): Boolean {
        return false
    }
}