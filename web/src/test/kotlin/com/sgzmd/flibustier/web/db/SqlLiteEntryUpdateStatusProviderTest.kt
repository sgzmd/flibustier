package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class SqlLiteEntryUpdateStatusProviderTest {
  @Autowired
  lateinit var connectionProvider: ConnectionProvider
  lateinit var entryUpdateStatusProvider: IEntryUpdateStatusProvider

  @Before
  fun setUp() {
    entryUpdateStatusProvider = SqlLiteEntryUpdateStatusProvider(connectionProvider)
  }

  @Test
  fun checkForUpdates_SeriesUpdateRequired() {
    val tracked = TrackedEntry(FoundEntryType.SERIES, "Whatever", 34145, 5)
    val result = entryUpdateStatusProvider.checkForUpdates(listOf(tracked))

    assertEquals(1, result.size)
    val updateRequired = result[0]
    assertEquals(34145, updateRequired.entry.entryId)
    assertEquals(10, updateRequired.newCount)
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
}
