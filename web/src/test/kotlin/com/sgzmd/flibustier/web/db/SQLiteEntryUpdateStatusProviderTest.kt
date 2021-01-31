package com.sgzmd.flibustier.web.db

import com.google.common.truth.Truth
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.internal.matchers.Equals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class SQLiteEntryUpdateStatusProviderTest {
  @Autowired
  lateinit var connectionProvider: ConnectionProvider

  @Autowired
  lateinit var repo: TrackedEntryRepository

  lateinit var entryUpdateStatusProvider: SQLiteEntryUpdateStatusProvider

  @Before
  fun setUp() {
    entryUpdateStatusProvider = SQLiteEntryUpdateStatusProvider(connectionProvider, repo)
  }

  @Test
  fun checkForUpdates_SeriesUpdateRequired() {
    val trackedBooks = listOf(
        Book("Чужие маски", 452501),
        Book("Теряя маски", 452502),
        Book("Меняя маски", 452503),
        Book("Унесенный ветром. Книга пятая", 497148),
        Book("Унесенный ветром. Книга 5", 501398),
        Book("Удерживая маску", 513628),
        Book("Срывая маски", 517316),
        Book("Маска зверя", 530624))

    val tracked = TrackedEntry(FoundEntryType.SERIES, "Whatever", 34145, trackedBooks.size)
    tracked.books = trackedBooks
    val result = entryUpdateStatusProvider.checkForUpdates(listOf(tracked))

    assertEquals(1, result.size)
    val updateRequired = result[0]
    assertEquals(34145, updateRequired.entry.entryId)
    assertEquals(10, updateRequired.newCount)

    val newBooks = updateRequired.newBooks
    val books = listOf(
        Book("Унесенный ветром: Меняя маски. Теряя маски. Чужие маски", 552329),
        Book("Осколки маски", 564438))

    Truth.assertThat(newBooks).containsExactlyElementsIn(books)
  }

  @Test
  fun checkForUpdates_AuthorUpdateRequired() {
    val tracked = TrackedEntry(FoundEntryType.AUTHOR, "Whatever", 109170, 3)
    val result = entryUpdateStatusProvider.checkForUpdates(listOf(tracked))

    assertEquals(1, result.size)
    val updateRequired = result[0]
    assertEquals(109170, updateRequired.entry.entryId)
    assertEquals(8, updateRequired.newCount)
  }


  @Test
  fun checkForUpdates_UpdateNotRequired() {
    val tracked = TrackedEntry(FoundEntryType.SERIES, "Whatever", 34145, 10)
    val result = entryUpdateStatusProvider.checkForUpdates(listOf(tracked))

    assertEquals(0, result.size)
  }

  @Test
  fun checkLukianenko() {
    val tracked = TrackedEntry(FoundEntryType.AUTHOR,
        entryName = "Сергей Лукьяненко",
        entryId = 1801,
        userId = "user@email.com",
        numEntries = 390)
    val result = entryUpdateStatusProvider.checkForUpdates(listOf(tracked))


    assertEquals(1, result.size)
    val updateRequired = result[0]
    assertEquals(1801, updateRequired.entry.entryId)
    assertEquals(392, updateRequired.newCount)
  }

  @Test
  fun checkBooksIncluded() {
    // The test data below was generated with this query:
    //
    //    select 'Book("' || Title || '",' || a.BookId || ')'
    //    from libbook b,
    //    libavtor a
    //        where b.BookId = a.BookId
    //        and a.AvtorId = 109170
    //    limit 8;
    val books = listOf(
        Book("Чужие маски", 452501),
        Book("Теряя маски", 452502),
        Book("Меняя маски", 452503),
        Book("Унесенный ветром. Книга пятая", 497148),
        Book("Унесенный ветром. Книга 5", 501398),
        Book("Удерживая маску", 513628),
        Book("Срывая маски", 517316),
        Book("Маска зверя", 530624))
    val entry = TrackedEntry(FoundEntryType.AUTHOR, "Маски", 109170, books.size, "test")
    entry.books = books

    val newBooks = entryUpdateStatusProvider.getNewBooks(entry)
    Truth.assertThat(newBooks).containsExactly(
        Book("Унесенный ветром: Меняя маски. Теряя маски. Чужие маски", 552329),
        Book("Осколки маски", 564438))
  }
}
