# How to send better emails

Right now when Flibustier discovers that there were some updates, it sends an
email which literally only has one line - the entity name which had updates.

To make this more useful, we need to show specifically _what_ are the new
things.

There are few ways to achieve it, but in all cases we will need to find out what
is the diff between old-state and new state.

## Option 1: diff on the actual databases

Requires storing previous version of the database somewhere, and then fetching
list of entities from the new database and the old database alike, comparing
them and finding which ones are new.

Database is fetched once a day, so we shouldn't be running into any race
conditions (in theory).

Major drawback - in Java code we have to be aware of physical data files, which
is not nice.

## Option 2: store list of sub-entities in the main app db.

In this option, we are storing full list of sub-entities (e.g. book names) in
the main MySQL database.

### Option 2.1

Store entities on a global basis - i.e. each time entity of type T and id N is
tracked, we create or obtain new non-user-affiliated entity and dump the line
items to it. Problems - sync between users, general awkwardness

### Option 2.2

Store list of line items directly on `TrackedEntity`. Potential duplication, but
given we are unlikely to ever reach the scale of more than 2-3 users, might be acceptable.