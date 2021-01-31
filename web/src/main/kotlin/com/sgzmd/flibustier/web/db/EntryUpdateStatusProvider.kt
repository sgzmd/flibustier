package com.sgzmd.flibustier.web.db

import com.google.common.annotations.VisibleForTesting
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.PreparedStatement

/**
 * For all practical purposes we can get rid of the interface and just use SqlLiteEntryUpdateStatusProvider instead.
 */
@Component
interface IEntryUpdateStatusProvider {
  data class UpdateRequired(val entry: TrackedEntry, val newCount: Int, val newBooks: List<Book>? = null)

  fun checkForUpdates(entries: List<TrackedEntry>) : List<UpdateRequired>
}

@Component
class SQLiteEntryUpdateStatusProvider(val connectionProvider: ConnectionProvider,
                                      val repo: TrackedEntryRepository) : IEntryUpdateStatusProvider {
  private val logger = LoggerFactory.getLogger(SQLiteEntryUpdateStatusProvider::class.java)

  val auditLog = LoggerFactory.getLogger("audit")


  fun forceReload() {
    connectionProvider.reload()
  }

  override fun checkForUpdates(entries: List<TrackedEntry>): MutableList<IEntryUpdateStatusProvider.UpdateRequired> {
    // SQLite3 file underneath might have changed by now.
    forceReload()

    // Finds all books for a given author (AvtorId)
    val sqlAuthor = "select count(1) Cnt from libavtor la, libbook lb where " +
        "lb.BookId = la.BookId and lb.Deleted != '1' and la.AvtorId = ?"

    // Finds all books for a given series (SeqId)
    val sqlSeries = "select count(ls.BookId) Cnt from libseq ls where ls.SeqId = ?"

    // We are grouping all tracked entries by its ID and type
    val byEntryId: Map<Pair<Int?, FoundEntryType?>, List<TrackedEntry>> =
        entries.groupBy { Pair(it.entryId, it.entryType) }

    val result = mutableListOf<IEntryUpdateStatusProvider.UpdateRequired>()
    val prsAuthor = connectionProvider.connection?.prepareStatement(sqlAuthor)
    val prsSeries = connectionProvider.connection?.prepareStatement(sqlSeries)

    auditLog.info("There are ${byEntryId.keys.size} keys in byEntryId")

    for (entry in byEntryId.keys) {
      logger.info("Evaluating updates for entryId=$entry")
      val group = byEntryId[entry]

      when (entry.second) {
        FoundEntryType.AUTHOR -> processEntriesOfType(group, FoundEntryType.AUTHOR, prsAuthor, entry.first, result)
        FoundEntryType.SERIES -> processEntriesOfType(group, FoundEntryType.SERIES, prsSeries, entry.first, result)
      }
    }

    return result
  }

  @VisibleForTesting
  fun getNewBooks(entry: TrackedEntry): List<Book> {
    // TODO: switch to proper prepared statements
    val stm = when (entry.entryType) {
      FoundEntryType.AUTHOR -> connectionProvider.connection?.prepareStatement(
          "select b.BookId, b.Title from libbook b, libavtor a where b.BookId = a.BookId and a.AvtorId = ?")
      FoundEntryType.SERIES -> connectionProvider.connection?.prepareStatement(
          "select b.BookId, b.Title from libbook b, libseq s where s.BookId = b.BookId and s.SeqId = ?")
      else -> throw IllegalArgumentException("Can only check updates for series and authors")
    }

    stm?.setInt(1, entry.entryId!!)

    val rs = stm?.executeQuery()!! // if ExecuteQuery returns null, things are terribly wrong

    val existingBooks = entry.books.groupBy { it.bookId }
    val newBooks = mutableListOf<Book>()
    while (rs.next()) {
      val b = Book(rs.getString("Title"), rs.getInt("BookId"))
      if (!existingBooks.containsKey(b.bookId)) {
        newBooks.add(b)
      }
    }

    return newBooks
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
    if (entriesOfType?.isNullOrEmpty()!!) {
      return
    }
    val entryName = entriesOfType.first().entryName

    if (resultSet?.next()!!) {
      val newCount = resultSet.getInt("Cnt")
      val toBeUpdated = entriesOfType.filter { it.numEntries < newCount }

      auditLog.info("For type=$type and entryId=$entryId ($entryName) there are ${toBeUpdated.size} records to be updated")

      toBeUpdated.forEach {
        val newBooks = getNewBooks(it)
        result.add(IEntryUpdateStatusProvider.UpdateRequired(it, newCount, newBooks = newBooks))
        it.numEntries = newCount

        // Now let's produce final list of all new books associated with the entry
        val allBooks = mutableListOf<Book>()
        allBooks.addAll(it.books)
        allBooks.addAll(newBooks)
        it.books = allBooks

        repo.save(it)
      }
    }
  }

}