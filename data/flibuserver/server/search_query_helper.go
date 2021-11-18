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

	SEQUENCE_QUERY_TEMPLATE = `
select	
	f.SeqName,
	f.Authors,
	f.SeqId,
	(select count(ls.BookId) from libseq ls where ls.SeqId = f.SeqId) NumBooks
from 
	sequence_fts f 
where f.sequence_fts match ("%s*")
	`
)

func CreateAuthorSearchQuery(author string) string {
	return fmt.Sprintf(AUTHOR_QUERY_TEMPLATE, author)
}

func CreateSequenceSearchQuery(seq string) string {
	return fmt.Sprintf(SEQUENCE_QUERY_TEMPLATE, seq)
}
