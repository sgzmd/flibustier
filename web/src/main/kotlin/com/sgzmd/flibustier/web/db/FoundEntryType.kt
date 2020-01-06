package com.sgzmd.flibustier.web.db

enum class FoundEntryType {
    SERIES,
    AUTHOR,
    BOOK;

    override fun toString(): String{
        return when (this) {
            SERIES -> "Серия"
            AUTHOR -> "Автор"
            BOOK -> "Книга"
        }
    }
}