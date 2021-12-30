package com.sgzmd.flibustier.web.config

import com.sgzmd.flibustier.proto.FlibustierGrpc
import com.sgzmd.flibustier.web.grpc.FlibustierFake
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class TestConfigGrpc {
    @Bean
    fun getFlibustierService() : FlibustierGrpc.FlibustierImplBase {
        return FlibustierFake()
    }
}