package com.sgzmd.flibustier.web.controllers.debug

import com.sgzmd.flibustier.web.test.FlibuserverInitializer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import org.springframework.web.servlet.view.RedirectView

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration
internal class ForceCheckUpdatesTest {
    companion object {
        var flibuserverInitializer = FlibuserverInitializer("../testutils/flibusta-test.db")

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            flibuserverInitializer.initializeFlibuserver()
        }

        @AfterClass
        @JvmStatic
        fun teardownClass() {
            flibuserverInitializer.rampDownServer()
        }
    }

    @Autowired
    lateinit var forceUpdateController: ForceCheckUpdates

    @Test
    @WithMockUser("testuser")
    fun testTrack_Series() {
        val result = forceUpdateController.info().url
        assertEquals("/", result)
   }
}