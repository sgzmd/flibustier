package com.sgzmd.flibustier.web.controllers

import com.sgzmd.flibustier.web.scheduled.UpdateChecker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ForceCheckUpdates {
    @Autowired
    lateinit var updateChecker: UpdateChecker

    @RequestMapping("/force-check-updates")
    fun info(model: Model) : String {
        updateChecker.checkUpdates()

        return "info"
    }
}