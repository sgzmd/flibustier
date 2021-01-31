package com.sgzmd.flibustier.web.db

import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class GlobalSearchTest {
  @Autowired lateinit var connectionProvider: ConnectionProvider
  @Autowired lateinit var globalSearch: GlobalSearch

  @Test
  fun testConnectionAndDb() {
    assertNotNull(connectionProvider)
    assertNotNull(connectionProvider.connection)

    val stm = connectionProvider.connection?.createStatement()
    val rs = stm?.executeQuery("select count(1) as cnt from libbook where 1")

    assertTrue(rs?.next()!!)
    assertEquals(634, rs.getInt("cnt"))
  }

  @Test
  fun testSearchSequence() {
    val results = globalSearch.getSeriesResults("Унес")
    assertEquals(1, results.size)
  }

  @Test
  fun testSearchAuthor() {
    val results = globalSearch.getAuthorResults("Метель")
    assertEquals(1, results.size)
  }
}