package com.sgzmd.flibustier.web.scheduled

import com.sgzmd.flibustier.web.db.ConnectionProvider
import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.IEntryUpdateStatusProvider
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

val testUserNotifier = Mockito.mock(UserNotifier::class.java)

@Profile("test")
@Component
class TestConfig {
  @Bean
  fun getUserNotifier() : UserNotifier {
    return testUserNotifier
  }
}

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class UpdateCheckerTest {
  @Autowired
  lateinit var repo: TrackedEntryRepository

  @Autowired @Value("\${flibusta.dburl}")
  lateinit var dbUrl: String
  @Autowired
  lateinit var connectionProvider: ConnectionProvider

  lateinit var entryUpdateProvider: IEntryUpdateStatusProvider

  @Before
  fun setUp() {
    entryUpdateProvider = Mockito.mock(IEntryUpdateStatusProvider::class.java)
    repo.deleteAll()
  }

  @Test
  fun checkUpdates_Series() {
    val entry = TrackedEntry(FoundEntryType.SERIES, "Test Series", 1, 2, "user_series")
    repo.save(entry)

    whenever(entryUpdateProvider.checkForUpdates(Mockito.anyList()))
        .thenReturn(listOf(IEntryUpdateStatusProvider.UpdateRequired(
            entry, 3)))

    val updateChecker = UpdateChecker(repo, entryUpdateProvider, testUserNotifier, dbUrl, connectionProvider)
    updateChecker.checkUpdates()

    verify(testUserNotifier).notifyUser("user_series", "Test Series")
  }

  @Test
  fun checkUpdates_Author() {
    val entry = TrackedEntry(FoundEntryType.AUTHOR, "Test Author", 1, 2, "user_author")
    repo.save(entry)

    whenever(entryUpdateProvider.checkForUpdates(Mockito.anyList()))
            .thenReturn(listOf(IEntryUpdateStatusProvider.UpdateRequired(
                    entry, 3)))

    val updateChecker = UpdateChecker(repo, entryUpdateProvider, testUserNotifier, dbUrl, connectionProvider)
    updateChecker.checkUpdates()

    verify(testUserNotifier).notifyUser("user_author", "Test Author")
  }
}
