package com.sgzmd.flibustier.web.db

import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner
import java.sql.Connection

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
internal class GlobalSearchTest {
  @Autowired lateinit var connectionProvider: ConnectionProvider

  @Before
  fun initDb() {
    val r = Runtime.getRuntime().exec("sqlite3 test.db < src/test/resources/testutils/flibusta-db-schema.sql")

    val schema = GlobalSearchTest::class.java.getResource("/testutils/flibusta-db-schema.sql").readText()
    val data = GlobalSearchTest::class.java.getResource("/testutils/flibusta-db-sample-data.sql").readText()
    val stm = connectionProvider.connection?.createStatement()
    stm?.addBatch(schema)
    println(stm?.executeBatch())
    stm?.close()

    val stmData = connectionProvider.connection?.createStatement()
    stmData?.addBatch(data)
    stmData?.executeBatch()
  }

  @Test
  fun testConnection() {
    assertNotNull(connectionProvider)
    assertNotNull(connectionProvider.connection)
  }
}