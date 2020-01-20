package com.sgzmd.flibustier.web.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

//@Configuration
//@EnableWebSecurity
//@Profile("!test")
class OAuth2LoginSecurityConfig1 : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/info", "/login*", "/login/oauth2", "/css*", "/js*", "/img").permitAll()
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
                .loginPage("/login/oauth2")
    }
}

@Configuration
@EnableWebSecurity
@Profile("!test")
class OAuth2LoginSecurityConfig() : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/info", "/login/oauth2", "/oauth2/*", "/css/*", "/js/*", "/*.css", "/images/*").permitAll()
            .anyRequest().authenticated()
            .and().oauth2Login()
                .loginPage("/login/oauth2")
            .and()
            .logout()
            .logoutUrl("/perform_logout")
            .deleteCookies("JSESSIONID")

//        http.headers().frameOptions()?.disable();
    }
}