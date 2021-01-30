package com.sgzmd.flibustier.web.controllers.debug

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration
internal class ForceCheckUpdatesTest {
    @Autowired
    lateinit var forceUpdateController: ForceCheckUpdates

    @Test
    @WithMockUser("testuser")
    fun testTrack_Series() {
        val result = forceUpdateController.info()
        assertEquals("force-check-updates", result)
   }
}