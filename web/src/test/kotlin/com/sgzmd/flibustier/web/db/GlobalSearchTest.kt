package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.web.test.FlibuserverInitializer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class GlobalSearchTest {
  @Autowired lateinit var connectionProvider: ConnectionProvider
  @Autowired lateinit var globalSearch: IGlobalSearch

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

  @Test
  fun testSearchSequence() {
    val results = globalSearch.search("Унес")
    assertEquals(1, results.size)
  }

  @Test
  fun testSearchAuthor() {
    val results = globalSearch.search("Метельский")
    assertEquals(2, results.size)
  }
}