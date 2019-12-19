package com.sgzmd.flibustier

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.PWA

/**
 * The main view contains a button and a click listener.
 */
@Route
// @PWA(name = "My Application", shortName = "My Application")
class MainView : VerticalLayout() {
  init {
    val search = TextField()
    search.label = "What are we searching for"
    add(search)
    val regex = Regex(".+\\/sequence\\/([0-9]+).*")
    val button = Button("Another Button") {
      val url = search.value
      if (regex.matches(url)) {
        val match = regex.matchEntire(url)
        val seqId = match?.groupValues?.get(1)
        if (null != seqId) {
          // Notification.show("Matched sequence " + seqId)
          val dp = DataProvider()
          Notification.show(dp.getSequenceName(1))
          return@Button
        }
      }

      Notification.show("No match found")
    }
    add(button)
  }
}