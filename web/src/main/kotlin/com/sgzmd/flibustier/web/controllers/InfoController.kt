package com.sgzmd.flibustier.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class InfoController {
    @RequestMapping("/info")
    fun info(model: Model) : String {
        return "info"
    }
}