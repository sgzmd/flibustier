package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.GlobalSearch
import com.sgzmd.flibustier.web.db.IGlobalSearch
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import com.sgzmd.flibustier.web.security.AuthenticationFacade
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.lang.RuntimeException

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration
class IndexControllerTest() {
  @Autowired lateinit var repo: TrackedEntryRepository
  @Autowired lateinit var authFacade: AuthenticationFacade

  lateinit var indexController: IndexController

  @Before
  fun setUp() {
    indexController = IndexController(FakeGlobalSearch(), repo, authFacade)
  }

  @Component
  class FakeGlobalSearch : IGlobalSearch {
    override fun search(searchTerm: String): List<GlobalSearch.SearchResult> {
      return mutableListOf()
    }
  }

  @Test
  fun testGlobalSearch() {
    indexController.globalSearch.search("abc")
  }
}