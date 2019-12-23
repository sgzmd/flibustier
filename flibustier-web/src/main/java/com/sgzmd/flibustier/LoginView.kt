package com.sgzmd.flibustier

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route

@Tag("sa-login-view")
@Route(value = LoginView.ROUTE)
@PageTitle("Login")
class LoginView : Component() {
    companion  object {
        const val ROUTE = "login"
    }
}