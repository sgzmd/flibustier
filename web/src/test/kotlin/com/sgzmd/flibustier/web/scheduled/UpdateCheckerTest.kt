package com.sgzmd.flibustier.web.scheduled

import com.google.common.truth.Truth
import com.sgzmd.flibustier.proto.TrackEntryResult
import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.IEntryUpdateStatusProvider
import com.sgzmd.flibustier.web.db.IEntryUpdateStatusProvider.UpdateRequired
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import com.sgzmd.flibustier.web.proto.toProtoEntry
import com.sgzmd.flibustier.web.test.FlibuserverInitializer
import org.junit.*
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.mockito.Mockito.`when` as whenever

var testUserNotifier = Mockito.mock(UserNotifier::class.java)

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

@Profile("test")
@Component
class TestConfig {
  @Bean
  fun getUserNotifier(): UserNotifier {
    return testUserNotifier
  }
}

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class UpdateCheckerTest {
  @Autowired
  lateinit var connectionProvider: ConnectionProvider

  lateinit var entryUpdateProvider: IEntryUpdateStatusProvider

  companion object {
    var flibuserverInitializer = FlibuserverInitializer("../testutils/flibusta-test.db")
  }

  @Before
  fun setUp() {
    flibuserverInitializer.initializeFlibuserver()

    testUserNotifier = Mockito.mock(UserNotifier::class.java)
    entryUpdateProvider = Mockito.mock(IEntryUpdateStatusProvider::class.java)

    connectionProvider?.reload()

  }

  @After
  fun teardown() {
    flibuserverInitializer.rampDownServer()
  }


  fun save(entry: TrackedEntry) {
    val protoEntry = entry.toProtoEntry()
    val result = connectionProvider?.flibustierServer?.trackEntry(protoEntry)

    Truth.assertThat(result?.result).isEqualTo(TrackEntryResult.TRACK_OK)
  }

  @Test
  fun checkUpdates_Series() {
    val entry = TrackedEntry(FoundEntryType.SERIES, "Test Series", 1, 2, "user_series")
    save(entry)

    val newBooks = listOf(Book("TestBook", 1))
    whenever(entryUpdateProvider.checkForUpdates(Mockito.anyList()))
      .thenReturn(
        listOf(
          UpdateRequired(
            entry,
            3,
            newBooks = newBooks
          )
        )
      )

    val updateChecker = UpdateChecker(entryUpdateProvider, testUserNotifier, connectionProvider)
    updateChecker.checkUpdates()

    val captor = ArgumentCaptor.forClass(Update::class.java)
    verify(testUserNotifier).notifyUser(captor.capture())
    verifyUserAndBookList("user_series", captor, newBooks)
  }

  @Test
  fun checkUpdates_Author() {
    val entry = TrackedEntry(FoundEntryType.AUTHOR, "Test Author", 1, 2, "user_author")
    entry.books = listOf(Book("Original Book", 0))
    save(entry)

    val newBookList = listOf(Book("TestBook", 1))
    whenever(entryUpdateProvider.checkForUpdates(Mockito.anyList()))
      .thenReturn(
        listOf(
          UpdateRequired(
            entry,
            3,
            newBooks = newBookList
          )
        )
      )

    val updateChecker =
      UpdateChecker(entryUpdateProvider, testUserNotifier, connectionProvider)
    updateChecker.checkUpdates()

    val captor = ArgumentCaptor.forClass(Update::class.java)
    verify(testUserNotifier).notifyUser(captor.capture())

    verifyUserAndBookList("user_author", captor, newBookList)
  }

  private fun verifyUserAndBookList(
    expectedUserId: String,
    captor: ArgumentCaptor<Update>,
    newBookList: List<Book>
  ) {
    assertEquals(expectedUserId, captor.value.userId)
    // There must be only 1 new entry here
    captor.value.entries.forEach {
      assertEquals(/* author_id */1, it.key.entryId)
      assertEquals(newBookList, it.value)
    }
  }

  @Test
  fun checkUpdates_Author_MultiBooks() {
    val entry = TrackedEntry(FoundEntryType.AUTHOR, "Test Author", 1, 2, "user_author_mb")
    save(entry)

    val newBooksList = listOf(
      Book("TestBook", 1),
      Book("TestBook2", 2),
      Book("TestBook2", 3)
    )
    whenever(entryUpdateProvider.checkForUpdates(Mockito.anyList()))
      .thenReturn(
        listOf(
          UpdateRequired(
            entry,
            3,
            newBooks = newBooksList
          )
        )
      )

    val updateChecker =
      UpdateChecker(entryUpdateProvider, testUserNotifier, connectionProvider)
    updateChecker.checkUpdates()

    val captor = ArgumentCaptor.forClass(Update::class.java)
    verify(testUserNotifier).notifyUser(captor.capture())
    verifyUserAndBookList("user_author_mb", captor, newBooksList)
  }
}
