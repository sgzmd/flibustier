package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.PreparedStatement

@Component
interface IEntryUpdateStatusProvider {
  data class UpdateRequired(val entry: TrackedEntry, val newCount: Int)

  fun checkForUpdates(entries: List<TrackedEntry>) : List<UpdateRequired>
}

@Component
class SqlLiteEntryUpdateStatusProvider(val connectionProvider: ConnectionProvider) : IEntryUpdateStatusProvider {
  private val logger = LoggerFactory.getLogger(SqlLiteEntryUpdateStatusProvider::class.java)

  override fun checkForUpdates(entries: List<TrackedEntry>): MutableList<IEntryUpdateStatusProvider.UpdateRequired> {
    val sqlAuthor = """
      select count(1) Cnt 
      from libavtor la, libbook lb 
      where lb.BookId = la.BookId
      and lb.Deleted != '1'
      and la.AvtorId = ?
    """.trimIndent()
    val sqlSeries = "select count(ls.BookId) Cnt from libseq ls where ls.SeqId = ?"
    val byEntryId: Map<Int?, List<TrackedEntry>> = entries.groupBy { it.entryId }
    val result = mutableListOf<IEntryUpdateStatusProvider.UpdateRequired>()
    val prsAuthor = connectionProvider.connection?.prepareStatement(sqlAuthor)
    val prsSeries = connectionProvider.connection?.prepareStatement(sqlSeries)
    for (entryId in byEntryId.keys) {
      logger.info("Evaluating updates for entryId=$entryId")
      val group = byEntryId[entryId]
      val type = FoundEntryType.AUTHOR

      // Processing authors
      processEntriesOfType(group, FoundEntryType.AUTHOR, prsAuthor, entryId, result)
      processEntriesOfType(group, FoundEntryType.SERIES, prsSeries, entryId, result)
    }

    return result
  }

  private fun processEntriesOfType(
          group: List<TrackedEntry>?,
          type: FoundEntryType,
          prs: PreparedStatement?,
          entryId: Int?,
          result: MutableList<IEntryUpdateStatusProvider.UpdateRequired>) {
    val entriesOfType = group?.filter { it.entryType == type }
    prs?.setInt(1, entryId!!)
    val resultSet = prs?.executeQuery()
    if (resultSet?.next()!!) {
      val newCount = resultSet?.getInt("Cnt")
      val toBeUpdated = entriesOfType?.filter { it.numEntries < newCount }
      toBeUpdated?.forEach {
        result.add(IEntryUpdateStatusProvider.UpdateRequired(it, newCount))
      }
    }
  }

}