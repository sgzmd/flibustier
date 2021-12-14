package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.proto.FlibustierGrpc
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class ConnectionProvider(
    @Value("\${flibusta.dburl}") dbUrl: String,
    @Value("\${flibusta.rpc.host}") host: String,
    @Value("\${flibusta.rpc.port}") port: Int,
) {
    private val logger = LoggerFactory.getLogger(ConnectionProvider::class.java)

    private val _host = host
    private val _port = port

    private var _connectionUrl = dbUrl
    private var _connection: Connection? = null
    var connection: Connection? = null
        @Synchronized get() {
            Class.forName("org.sqlite.JDBC")
            if (_connection == null) {
                logger.info("Opening connection to $_connectionUrl")
                _connection = DriverManager.getConnection(_connectionUrl)
            }

            return _connection
        }

    @Synchronized
    fun reload() {
        logger.info("Force closing connection")
        _connection?.close()
        _connection = null
        _flibustierStub = null
    }

    private var _flibustierStub: FlibustierGrpc.FlibustierBlockingStub? = null
    var flibustierServer: FlibustierGrpc.FlibustierBlockingStub? = null
        @Synchronized get() {
            if (_flibustierStub == null) {
                val channel = ManagedChannelBuilder.forAddress(_host, _port).usePlaintext().build()
                _flibustierStub = FlibustierGrpc.newBlockingStub(channel)
            }

            return _flibustierStub
        }

    fun getLastUpdateTimestamp(): LocalDateTime {
        val dbFile = File(getFileNameFromUrl(_connectionUrl))
        val lastModified = dbFile.lastModified()
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), TimeZone.getTimeZone("UTC").toZoneId())
    }

    private fun getLastAfterDelimiter(url: String, delimiter: String): String {
        val index = url.lastIndexOf(delimiter)
        return url.substring(index + 1)
    }

    // URL can be like
    // dburl: "jdbc:sqlite:/opt/apps/flibustier/flibusta.db"
    internal fun getFileNameFromUrl(url: String): String {
        return getLastAfterDelimiter(url, ":")
    }
}
