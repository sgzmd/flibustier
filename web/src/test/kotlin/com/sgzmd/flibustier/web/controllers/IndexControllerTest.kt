package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.GlobalSearch
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringJUnitWebConfig
class IndexControllerTest() {
  @Autowired lateinit var wac: WebApplicationContext

  lateinit var mockMvc: MockMvc

  @Before
  fun setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
  }

  @Test
  @WithMockUser("testuser")
  fun testGlobalSearch() {
    mockMvc.get("/?search_term=Унес").andExpect {
      status { isOk }
      model {
        attribute("searchResults",
            Matchers.equalTo(arrayListOf(
                GlobalSearch.SearchResult(
                    FoundEntryType.SERIES,
                    name = "Унесенный ветром",
                    entryId = 34145,
                    numEntities = 10))))
      }
    }
  }
}