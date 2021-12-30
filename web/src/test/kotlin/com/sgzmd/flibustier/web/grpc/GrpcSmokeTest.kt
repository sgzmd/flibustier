package com.sgzmd.flibustier.web.grpc

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.sgzmd.flibustier.proto.FlibustierGrpc
import com.sgzmd.flibustier.proto.ListTrackedEntriesRequest
import io.grpc.inprocess.InProcessChannelBuilder
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringJUnitWebConfig
@SpringBootTest(properties = ["grpc.server.inProcessName=test"])
@DirtiesContext
class GrpcSmokeTest {
    @Autowired
    private lateinit var stub: FlibustierGrpc.FlibustierBlockingStub

    @Test
    fun testSmoke() {
        val result = stub.listTrackedEntries(ListTrackedEntriesRequest.newBuilder().build())
        assertThat(result.entryList).isNotEmpty()
    }
}