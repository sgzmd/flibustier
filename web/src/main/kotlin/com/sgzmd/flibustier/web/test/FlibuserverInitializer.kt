package com.sgzmd.flibustier.web.test

class FlibuserverInitializer(flibustaDb: String) {
    val _flibustaDb = flibustaDb
    var _process: Process? = null
    fun initializeFlibuserver() {
        val dir = System.getProperty("user.dir")
        _process = ProcessBuilder("../data/flibustier_server", "-port", "9000", "-flibusta_db",  _flibustaDb)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
    }

    fun rampDownServer() {
        if (_process != null) {
            _process?.destroy()
        }
    }
}