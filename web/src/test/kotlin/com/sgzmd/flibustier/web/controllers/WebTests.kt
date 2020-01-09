package com.sgzmd.flibustier.web.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.event.EventListener
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

/**
 * Main goal of this test is to check that the server can start and the login page renders.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebTests {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int? = null
    @EventListener(WebServerInitializedEvent::class)
    fun onServletContainerInitialized(event: WebServerInitializedEvent) {
        port = event.webServer.port
    }

    @Test
    fun testRenderPage() {
        assertThat(restTemplate.getForObject("http://localhost:$port/", String::class.java)).contains("Флибустьер: вход")
    }
}