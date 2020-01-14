package com.sgzmd.flibustier.web.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotEmpty


abstract class BaseSecurityConfigurerAdapter : WebSecurityConfigurerAdapter() {
  override fun configure(http: HttpSecurity?) {
    http
        ?.csrf()?.disable()
        ?.authorizeRequests()
        ?.antMatchers("/info", "/login*", "/css/*", "/js/*", "/*.css")?.permitAll()
        ?.anyRequest()?.authenticated()
        ?.and()
        ?.formLogin()
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

@Configuration
@EnableWebSecurity
@Profile("dev")
class DevSecurityConfig : BaseSecurityConfigurerAdapter() {
  @Throws(Exception::class)
  override fun configure(auth: AuthenticationManagerBuilder) {
    auth.inMemoryAuthentication()
        .withUser("sgzmd").password(passwordEncoder()!!.encode("123")).roles("USER")
  }
  @Bean
  fun passwordEncoder(): PasswordEncoder? {
    return BCryptPasswordEncoder()
  }
}

@Configuration
@EnableWebSecurity
@Profile("prod")
class ProdSecurityConfig : BaseSecurityConfigurerAdapter() {

}

@Entity(name = "users")
class User : Serializable {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  var userId: Int = 0

  @NotEmpty @Column(nullable = false, unique = true)
  var userName: String = ""

  @NotEmpty
  var passwordHash: String = ""
}