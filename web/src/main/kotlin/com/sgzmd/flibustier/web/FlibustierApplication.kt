package com.sgzmd.flibustier.web

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlibustierApplication

val auditLog = LoggerFactory.getLogger("audit")

fun main(args: Array<String>) {
	auditLog.info("Starting Flibustier...")
	runApplication<FlibustierApplication>(*args)
}
