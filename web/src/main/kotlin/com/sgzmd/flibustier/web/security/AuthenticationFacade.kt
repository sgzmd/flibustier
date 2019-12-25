package com.sgzmd.flibustier.web.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {
  fun authentication() : Authentication {
    return SecurityContextHolder.getContext().authentication
  }

  fun getUserId() : String {
    return (authentication().principal as User).username
  }
}