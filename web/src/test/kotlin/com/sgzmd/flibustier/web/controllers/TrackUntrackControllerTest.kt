package com.sgzmd.flibustier.web.controllers

import com.google.common.truth.Truth
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import com.sgzmd.flibustier.web.test.FlibuserverInitializer
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import com.google.common.truth.Truth.assertThat as assertThat


@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration
internal class TrackUntrackControllerTest {
  @Autowired
  lateinit var trackController: TrackController
  @Autowired
  lateinit var untrackController: UntrackController

  @Autowired
  lateinit var repo: TrackedEntryRepository

  companion object {
    var flibuserverInitializer = FlibuserverInitializer("../testutils/flibusta-test.db")

    @BeforeClass
    @JvmStatic
    fun setUpClass() {
      flibuserverInitializer.initializeFlibuserver()
    }

    @AfterClass
    @JvmStatic
    fun teardownClass() {
      flibuserverInitializer.rampDownServer()
    }
  }

  @Before
  fun setUp() {
    repo.deleteAll()
  }

  @Test
  @WithMockUser("testuser")
  fun testTrack_Series() {
    val result = trackController.track(34145, "", FoundEntryType.SERIES)
    assertEquals("/", result.url)
    val records = repo.findByEntryId(34145)
    assertEquals(1, records.size)
    val entry = records[0]

    assertEquals("Унесенный ветром", entry.entryName)
    assertThat(entry.books).hasSize(8)
  }

  @Test
  @WithMockUser("testuser")
  fun testUntrack_Series() {
    trackController.track(34145, "", FoundEntryType.SERIES)
    val record = repo.findByEntryId(34145)[0]
    untrackController.untrack(record.id)
    assertEquals(0, repo.findAll().count())
  }

  @Test
  @WithMockUser("testuser")
  fun testTrack_Author() {
    val result = trackController.track(109170, "", FoundEntryType.AUTHOR)
    assertEquals("/", result.url)
    val records = repo.findByEntryId(109170)
    assertEquals(1, records.size)
    val entry = records[0]

    assertEquals("Николай Александрович Метельский", entry.entryName)
    assertThat(entry.books).hasSize(8)
  }

  @Test
  @WithMockUser("testuser")
  fun testUntrack_Author() {
    repo.save(TrackedEntry(FoundEntryType.AUTHOR, "", 109170, 10, "testuser"))
    val record = repo.findByEntryId(109170)[0]
    untrackController.untrack(record.id)
    val all = repo.findAll()
    assertEquals(0, all.count())
  }

  @Test
  @WithMockUser("testuser")
  fun testTrackSeries_WasFailingNoId() {
    val entry = TrackedEntry(FoundEntryType.SERIES, "", 60679, 10, "testuser")
    val books = mutableListOf<Book>()
    books.add(Book("Найденыш", 577776))
    books.add(Book("Притяжение силы", 609214))
    entry.books = books
    repo.save(entry)
    val record = repo.findByEntryId(60679)[0]
    untrackController.untrack(record.id)
    val all = repo.findAll()
    assertEquals(0, all.count())
  }
}