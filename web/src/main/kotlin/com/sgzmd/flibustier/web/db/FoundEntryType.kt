package com.sgzmd.flibustier.web.db

import com.sgzmd.flibustier.proto.EntryType

enum class FoundEntryType {
    SERIES,
    AUTHOR,
    BOOK;
}

class EntryTypeConverter {
    companion object {
        fun fromProto(protoEntryType: EntryType) : FoundEntryType {
            return when (protoEntryType) {
                EntryType.AUTHOR -> FoundEntryType.AUTHOR
                EntryType.BOOK -> FoundEntryType.BOOK
                EntryType.SERIES -> FoundEntryType.SERIES

                // Practically never used
                else -> FoundEntryType.SERIES
            }
        }

        fun toProto(dbEntryType: FoundEntryType) :EntryType {
            return when (dbEntryType) {
                FoundEntryType.AUTHOR -> EntryType.AUTHOR
                FoundEntryType.SERIES -> EntryType.SERIES
                FoundEntryType.BOOK -> EntryType.BOOK
            }
        }
    }
}
