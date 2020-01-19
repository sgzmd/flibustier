package com.sgzmd.flibustier.web.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {
  fun authentication() : Authentication {
    return SecurityContextHolder.getContext().authentication
  }

  fun getUserId() : String {
    return when (val principal = authentication().principal) {
      is User -> {
        principal.username
      }
      is OAuth2User -> {
        val oauth2User = principal
        oauth2User.attributes["email"] as String
      }
      else -> {
        throw RuntimeException("Unsupported user type: ${principal.javaClass.name}")
      }
    }
  }
}