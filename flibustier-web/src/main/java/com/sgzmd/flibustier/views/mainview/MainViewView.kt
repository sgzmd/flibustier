package com.sgzmd.flibustier.views.mainview

import com.sgzmd.flibustier.MainView
import com.sgzmd.flibustier.backend.SequenceService
import com.sgzmd.flibustier.backend.Series
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import org.springframework.beans.factory.annotation.Autowired

@Route(value = "main", layout = MainView::class)
@RouteAlias(value = "", layout = MainView::class)
@PageTitle("MainView")
@CssImport("styles/views/mainview/main-view-view.css")
class MainViewView(@Autowired sequenceService: SequenceService) : Div() {
    init {
        setId("main-view-view")

        val layout = VerticalLayout()

        layout.setClassName("vertical-layout")
        val header = Label("Flibustier")
        header.setClassName("header")
        layout.add(header)

        val combo = ComboBox<Series>()
        combo.setClassName("instant-search-box", true)
        combo.setDataProvider(sequenceService::fetch, sequenceService::numEntries)
        combo.addValueChangeListener {
            Notification.show("Value changed from ${it.oldValue} to ${it.value} (seqId=${it.value.seqId})!")
        }

        layout.add(combo)
        add(layout)
    }
}
