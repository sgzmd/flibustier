package com.sgzmd.flibustier.web.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UserIdProvider(@Value("\${dev.userId}") var userId: Int) {

}
