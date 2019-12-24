package com.sgzmd.flibustier.web.db

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class ConnectionProvider(@Value("flibusta.db") dbUrl: String) {
    private var _connectionUrl = dbUrl
    private var _connection: Connection? = null
    var connection : Connection? = null
    @Synchronized get() {
        if (_connection == null) {
            _connection = DriverManager.getConnection(_connectionUrl)
        }

        return _connection
    }
}