# Flibustier v2 High Level Plan

## New plan

There are 2 things one can follow:

1. Sequence
2. Author

This really sums it up. 

There will be following operations:

1. Add to list of follows
2. Check all follows for updates

### Follow something

When we want to follow something, we want to:

1. Check it's not already followed. This requires <id,type> scan.
2. Create a follow

## Old Content

The problem of Flibustier v1 is that it became so messy that the migration to new model (where we stored which books)
were there became very hard to the point that I no longer understand how the code works.

Therefore, the goal of v2 is to build it upon a simple architecture that makes sense from the get go. High cohesion,
low coupling all the way. 

For starters we don't need need a web UI - but having a separate client and a server is a must.

## Dump downloader
Should work much in the same way, with the caveat that using sqlite3 was a mistake. We should just use simple 
encapsulated MySQL which will make overall migration easier.

Or maybe not? Was there anything wrong with sqlite? 

Or should we just import everything straight away to Mongo? There may be downtime involved though.

Let's think how schema might look like:

```javascript

Author = {
    "name": "Author Name",
     books: [
         {
             "book_name": "Book Name",
             "series": "Series Name"
         }
     ] 
}
```

But what if book belongs to several authors? MongoDB's answer is to denormalize it so that:

```json
[{
  "_id": 1,
  "name": "Peter Standford",
  "books": [1, 2]
},
{
  "_id": 2,
  "name": "Georg Peterson",
  "books": [2]
}]
```

To simplify the whole thing, we can start with sqlite3 database, which we'll read in Go and convert to Mongo. This will
act as a gentle introduction to both :)

Can we denormalize this even further to use some effective key-value store
instead of a "proper" database?

```
# books
key = "1234"
value = "proto:book"

```

