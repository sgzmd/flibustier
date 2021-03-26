package main

import (
	"flag"
	"log"

	"flibustier_v2/internal/search"
)

func main() {
	kvRoot := flag.String("kv_root", "kv", "Root of KV store")
	searchFor := flag.String("search", "book:.+дозор.+", "What are we searching for")
	searchType := flag.String("type", "book", "Search type (id/query)")

	flag.Parse()

	stype, err := search.ParseSearchType(*searchType)
	if err != nil {
		log.Panic(err)
	}

	q, err := search.ParseQuery(*searchFor, stype)
	if err != nil {
		log.Panic(err)
	}


}
