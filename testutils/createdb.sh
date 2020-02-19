#!/bin/bash -x

rm flibusta-test.db
sqlite3 flibusta-test.db < flibusta-db-schema.sql
sqlite3 flibusta-test.db < flibusta-db-sample-data.sql
sqlite3 flibusta-test.db < luk.sql
sqlite3 flibusta-test.db < ../import/SequenceAuthor.sql
