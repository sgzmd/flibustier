package com.sgzmd.flibustier.web.scheduled

import com.sgzmd.flibustier.web.db.IEntryUpdateStatusProvider
import com.sgzmd.flibustier.web.db.SqlLiteEntryUpdateStatusProvider
import com.sgzmd.flibustier.web.db.TrackedEntryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UpdateChecker(
    @Autowired val trackedEntryRepository: TrackedEntryRepository,
    @Autowired val entryUpdateStatusProvider: IEntryUpdateStatusProvider) {

}
