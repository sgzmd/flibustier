package com.sgzmd.flibustier.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
class LoginController {
    @RequestMapping("/login/oauth2")
    fun info(model: Model) : String {
        return "login_oauth2"
    }

    @RequestMapping("/sw.js")
    fun crutch() : RedirectView {
        return RedirectView("/")
    }
}