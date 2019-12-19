package com.sgzmd.flibustier

import java.sql.Connection
import java.sql.DriverManager

class DataProvider {
  var connection: Connection? = null

  init {
    val FILE_NAME = "/home/sgzmd/code/flibustier/data/flibusta.db"
    val DB_URL = "jdbc:sqlite:" + FILE_NAME

    connection = DriverManager.getConnection(DB_URL)
  }

  fun getSequenceName(seqId: Int) : String {
    return "Test"
  }
}