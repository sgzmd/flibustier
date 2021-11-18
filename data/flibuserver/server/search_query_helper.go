package main

import "fmt"

const (
	AUTHOR_QUERY_TEMPLATE = `
select 
	a.authorName, 
	a.authorId,
	COUNT(1) as Count
from 
	author_fts a,
	libavtor la,
	libbook lb
where     
	a.author_fts match("%s*")
	and la.AvtorId = a.authorId
	and la.BookId = lb.BookId
	and lb.Deleted != '1'
GROUP BY 1,2;
	`
)

func CreateAuthorSearchQuery(author string) string {
	return fmt.Sprintf(AUTHOR_QUERY_TEMPLATE, author)
}
