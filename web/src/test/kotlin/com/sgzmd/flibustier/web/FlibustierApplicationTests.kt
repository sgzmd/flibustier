package com.sgzmd.flibustier.web

import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import java.util.*


@SpringBootTest
class FlibustierApplicationTests

@Configuration
@Profile("test")
class OAuth2LoginSecurityConfig : WebSecurityConfigurerAdapter() {
  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http
        .authorizeRequests().anyRequest().authenticated().and()
        .oauth2Login(Customizer {
          it.tokenEndpoint(Customizer {
            it.accessTokenResponseClient(mockAccessTokenResponseClient())
          })
        })
  }

  private fun mockAccessTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest?>? {
    val accessTokenResponse = OAuth2AccessTokenResponse.withToken("access-token-1234")
        .tokenType(OAuth2AccessToken.TokenType.BEARER)
        .expiresIn(60 * 1000.toLong())
        .build()
    val tokenResponseClient: OAuth2AccessTokenResponseClient<*> = mock(OAuth2AccessTokenResponseClient::class.java)
    `when`(tokenResponseClient.getTokenResponse(Mockito.any())).thenReturn(accessTokenResponse)

    return tokenResponseClient as OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest?>?
  }

  private fun mockUserService(): OAuth2UserService<OAuth2UserRequest?, OAuth2User?>? {
    val attributes: MutableMap<String, Any> = HashMap()
    attributes["id"] = "joeg"
    attributes["first-name"] = "Joe"
    attributes["last-name"] = "Grandja"
    attributes["email"] = "joeg@springsecurity.io"
    val authority: GrantedAuthority = OAuth2UserAuthority(attributes)
    val authorities: MutableSet<GrantedAuthority> = HashSet()
    authorities.add(authority)
    val user = DefaultOAuth2User(authorities, attributes, "email")
    val userService: OAuth2UserService<*, *> = mock(OAuth2UserService::class.java)
    `when`(userService.loadUser(Mockito.any())).thenReturn(user)
    return userService as OAuth2UserService<OAuth2UserRequest?, OAuth2User?>?
  }
}