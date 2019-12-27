package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
interface IEntryUpdateStatusProvider {
  data class UpdateRequired(val entry: TrackedEntry, val newCount: Int)

  fun checkForUpdates(entries: List<TrackedEntry>) : List<UpdateRequired>
}

@Component
class SqlLiteEntryUpdateStatusProvider(val connectionProvider: ConnectionProvider) : IEntryUpdateStatusProvider {
  private val logger = LoggerFactory.getLogger(SqlLiteEntryUpdateStatusProvider::class.java)

  override fun checkForUpdates(entries: List<TrackedEntry>)  : List<IEntryUpdateStatusProvider.UpdateRequired> {
    val sql = "select count(ls.BookId) Cnt from libseq ls where ls.SeqId = ?"
    val byEntryId: Map<Int?, List<TrackedEntry>> = entries.groupBy { it.entryId }

    val result = mutableListOf<IEntryUpdateStatusProvider.UpdateRequired>()

    val prs = connectionProvider.connection?.prepareStatement(sql)
    for (entryId in byEntryId?.keys) {
      logger.info("Evalutating updates for $entryId")
      val group: List<TrackedEntry>? = byEntryId[entryId]

      // we only support series at the moment
      val trackedSeriesEntries: List<TrackedEntry>? = group?.filter { it.entryType == FoundEntryType.SERIES }
      prs?.setInt(1, entryId!!)
      val rs = prs?.executeQuery()

      if (rs?.next()!!) {
        val newCount = rs?.getInt("Cnt")
        val toBeUpdated = trackedSeriesEntries?.filter { it.numEntries < newCount }

        logger.info("New count for $entryId is $newCount, ${toBeUpdated?.size} records to be updated")
        toBeUpdated?.forEach { result.add(IEntryUpdateStatusProvider.UpdateRequired(it, newCount)) }
      }
    }

    return result
  }
}