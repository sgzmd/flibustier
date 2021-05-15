package com.sgzmd.flibustier.web.scheduled

import com.sgzmd.flibustier.web.db.FoundEntryType
import com.sgzmd.flibustier.web.db.entity.Book
import com.sgzmd.flibustier.web.db.entity.TrackedEntry
import javax.mail.internet.MimeMessage
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
class EmailUserNotifierTest {
  var mailSender: JavaMailSender = Mockito.mock(JavaMailSender::class.java)
  var mimeMessage: MimeMessage = Mockito.mock(MimeMessage::class.java)

  @Before
  fun setUp() {
    mailSender = Mockito.mock(JavaMailSender::class.java)
    mimeMessage = Mockito.mock(MimeMessage::class.java)

    Mockito.`when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
  }

  @Test
  fun testEmailNotifier() {
    var called = false
    val emailNotifier = object : EmailUserNotifier(mailSender) {
      override fun sendMessage(userId: String, updateText: String) {
        assertEquals(
          """<html><body>
            |<h1>SERIES: TestEntry</h1>
            |<ul>
            |<li><a href='https://flibusta.is/b/123'>NewBook</a></li>
            |</ul>
            |</body>
            |</html>""".trimMargin().replace("\n", ""),
          updateText
        )
        called = true
      }
    }
    emailNotifier.notifyUser(
      Update(
        "test", mapOf(
          Pair(
            TrackedEntry(FoundEntryType.SERIES, "TestEntry", 1, 1, "test"), listOf(
              Book("NewBook", 123)
            )
          )
        )
      )
    )
    assertTrue(called)
  }
}