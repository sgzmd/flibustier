package com.sgzmd.flibustier.web.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfigurationAdapter : WebSecurityConfigurerAdapter() {
    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.inMemoryAuthentication()
                ?.withUser("sgzmd")?.password(passwordEncoder()?.encode("pwd"))
                ?.authorities("ROLE_USER")
    }

    override fun configure(http: HttpSecurity?) {
//        http
//                ?.authorizeRequests()
//                ?.anyRequest()?.authenticated()
//                ?.and()
//                ?.formLogin()?.loginPage("/login")?.permitAll()
        // nothing
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder? {
        return BCryptPasswordEncoder()
    }
}