package com.sgzmd.flibustier.web.controllers.debug

import com.sgzmd.flibustier.web.scheduled.UpdateChecker
import com.sgzmd.flibustier.web.scheduled.UserNotifier
import com.sgzmd.flibustier.web.security.AuthenticationFacade
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
class ForceCheckUpdates(@Autowired val updateNotifier: UserNotifier, @Autowired val authFacade: AuthenticationFacade) {
    @Autowired
    lateinit var updateChecker: UpdateChecker

    private val logger = LoggerFactory.getLogger(ForceCheckUpdates::class.java)

    @RequestMapping("/force-check-updates")
    fun info() : RedirectView {
        logger.info("Force checking updates ...")
        updateChecker.checkUpdates()
        logger.info("Trying to send email to ${authFacade.getUserId()}...")
        updateNotifier.notifyUser(authFacade.getUserId(), "Hello world!!")
        logger.info("Email should be sent.")
        return RedirectView("/")
    }
}