package com.sgzmd.flibustier.web.proto

import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import java.io.File

/**
 * Until I can come up with a sensible way of re-using exact same proto file in golang and java projects, I'll have
 * to ensure that at least it's identical.
 */
@RunWith(BlockJUnit4ClassRunner::class)
class ProtoEquivalenceTest {
    @Test
    fun testProtoEquivalence() {
        val originalProtoFile = File("../data/flibuserver/proto/flibustier.proto").readText()
        val copyProtoFile = File("src/main/proto/flibustier.proto").readText()

        Truth.assertThat(originalProtoFile).isEqualTo(copyProtoFile)
    }
}