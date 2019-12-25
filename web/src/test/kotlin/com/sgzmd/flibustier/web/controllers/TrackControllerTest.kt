package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner


@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration
internal class TrackControllerTest {
  @Autowired
  lateinit var controller: TrackController

  @Autowired
  lateinit var repo: TrackedEntryRepository

  @Test
  @WithMockUser("testuser")
  fun track() {
    val result = controller.track(34145 as Int, "", FoundEntryType.SERIES)
    assertEquals("/", result.url)
    val records = repo.findByEntryId(34145)
    assertEquals(1, records.size)
    val entry = records[0]

    assertEquals("Унесенный ветром", entry.entryName)
  }
}