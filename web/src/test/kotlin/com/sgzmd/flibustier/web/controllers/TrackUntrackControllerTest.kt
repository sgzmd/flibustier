package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.GlobalSearch
import com.sgzmd.flibustier.web.db.IGlobalSearch
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.registerBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner


@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration
internal class TrackUntrackControllerTest {
  @Autowired
  lateinit var trackController: TrackController
  @Autowired
  lateinit var untrackController: UntrackController

  @Autowired
  lateinit var repo: TrackedEntryRepository

  @Before
  fun setUp() {
    repo.deleteAll()
  }

  @Test
  @WithMockUser("testuser")
  fun testTrack_Series() {
    val result = trackController.track(34145 as Int, "", FoundEntryType.SERIES)
    assertEquals("/", result.url)
    val records = repo.findByEntryId(34145)
    assertEquals(1, records.size)
    val entry = records[0]

    assertEquals("Унесенный ветром", entry.entryName)
  }

  @Test
  @WithMockUser("testuser")
  fun testUntrack_Series() {
    trackController.track(34145 as Int, "", FoundEntryType.SERIES)
    val record = repo.findByEntryId(34145)[0]
    untrackController.untrack(record.id)
    assertEquals(0, repo.findAll().count())
  }

  @Test
  @WithMockUser("testuser")
  fun testTrack_Author() {
    val result = trackController.track(109170 as Int, "", FoundEntryType.AUTHOR)
    assertEquals("/", result.url)
    val records = repo.findByEntryId(109170)
    assertEquals(1, records.size)
    val entry = records[0]

    assertEquals("Николай Александрович Метельский", entry.entryName)
  }
}