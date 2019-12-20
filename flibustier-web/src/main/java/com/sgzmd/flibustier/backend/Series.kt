package com.sgzmd.flibustier.backend

data class Series(val sequenceName: String, val authors: String, val seqId: Int) {
    override fun toString(): String {
        return "$sequenceName - $authors"
    }
}
