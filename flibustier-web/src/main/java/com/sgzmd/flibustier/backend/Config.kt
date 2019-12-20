package com.sgzmd.flibustier.backend

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection
import java.sql.DriverManager

@Configuration
open class AppConfig {
    @Bean
    open fun sqlConnection() : Connection {
        val FILE_NAME = "/home/sgzmd/code/flibustier/data/flibusta.db"
        val DB_URL = "jdbc:sqlite:" + FILE_NAME
        return DriverManager.getConnection(DB_URL)
    }
}