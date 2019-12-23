package com.sgzmd.flibustier.backend;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //
        return authentication != null //
                && !(authentication instanceof AnonymousAuthenticationToken) //
                && authentication.isAuthenticated(); //
    }
}
