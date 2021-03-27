package main

import (
	"flag"
	"fmt"
	"log"

	"flibustier_v2/internal/search"
)

func main() {
	kvRoot := flag.String("kv_root", "kv", "Root of KV store")
	searchFor := flag.String("search", "book:.+дозор.+", "What are we searching for")
	searchType := flag.String("type", "name", "Search type (id/name)")

	flag.Parse()

	stype, err := search.ParseSearchType(*searchType)
	if err != nil {
		log.Panic(err)
	}

	q, err := search.ParseQuery(*searchFor, stype)
	if err != nil {
		log.Panic(err)
	}

	res, err := search.Search(*kvRoot, q)
	if err != nil {
		log.Panic(err)
	}

	if len(res.FoundAuthors) > 0 {
		fmt.Print("Authors found: \n")
		for idx, author := range res.FoundAuthors {
			fmt.Printf(
				"%d) id=% %s %s %s\n",
				idx,
				author.FlibustaAuthorId,
				author.FirstName,
				author.MiddleName,
				author.LastName,
			)
		}
	}

	if len(res.FoundSeqs) > 0 {
		fmt.Print("Sequences found: \n")
		for idx, seq := range res.FoundSeqs {
			fmt.Printf("%d) id=%s %s \n", idx, seq.FlibustaSequenceId, seq.SequenceName)
		}
	}

	if len(res.FoundBooks) > 0 {
		fmt.Print("Books found: \n")
		for idx, book := range res.FoundBooks {
			fmt.Printf("%d) id=%s %s \n", idx, book.FlibustaBookId, book.Title)
		}
	}
}
