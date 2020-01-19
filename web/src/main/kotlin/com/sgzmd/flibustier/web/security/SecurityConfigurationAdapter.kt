package com.sgzmd.flibustier.web.security

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@Profile("!test")
class OAuth2LoginSecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers("/info", "/login*", "/css*", "/js*").permitAll()
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
    }
}

//@Configuration
//@EnableWebSecurity
class SecurityConfigurationAdapter : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity?) {
        http
            ?.csrf()?.disable()
            ?.authorizeRequests()
            ?.antMatchers("/info", "/login*", "/css/*", "/js/*", "/*.css")?.permitAll()
            ?.anyRequest()?.authenticated()
            ?.and()
            ?.oauth2Login()
            ?.loginPage("/login")
            ?.permitAll()
            ?.and()
            ?.logout()
            ?.logoutUrl("/perform_logout")
            ?.deleteCookies("JSESSIONID")
            ?.permitAll();

        http?.headers()?.frameOptions()?.disable();
    }
}