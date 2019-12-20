package com.sgzmd.flibustier.views.mainview

import com.sgzmd.flibustier.MainView
import com.sgzmd.flibustier.backend.Series
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import org.springframework.beans.factory.annotation.Autowired

@Route(value = "main", layout = MainView::class)
@RouteAlias(value = "", layout = MainView::class)
@PageTitle("MainView")
@CssImport("styles/views/mainview/main-view-view.css")
class MainViewView(@Autowired sequenceService: DataProvider<Series, String>) : Div() {
    init {
        setId("main-view-view")

        val combo = ComboBox<Series>()
        combo.setDataProvider(sequenceService)
        add(combo)
    }
}
