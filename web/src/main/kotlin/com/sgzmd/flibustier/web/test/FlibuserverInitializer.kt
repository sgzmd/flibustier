package com.sgzmd.flibustier.web.test

import org.slf4j.LoggerFactory

class FlibuserverInitializer(flibustaDb: String) {
    val _flibustaDb = flibustaDb
    var _process: Process? = null
    private val logger = LoggerFactory.getLogger(FlibuserverInitializer::class.java)

    fun initializeFlibuserver() {
        logger.info("Initializing flibustier_server...")
        _process = ProcessBuilder("../data/flibustier_server", "-port", "9000", "-flibusta_db",  _flibustaDb)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
        logger.info("flibustier_server should be initialized")
    }

    fun rampDownServer() {
        if (_process != null) {
            logger.info("destroying flibustier_server")
            _process?.destroy()
        }
    }
}