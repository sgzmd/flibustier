package main

import (
	"flag"
	"log"
	"strings"
)

func main() {
	kvRoot := flag.String("kv_root", "kv", "Root of KV store")
	searchFor := flag.String("search", "book:.+дозор.+", "What are we searching for")

	flag.Parse()

	search := strings.Split(strings.ToLower(*searchFor), ":")
	if len(search) != 2 {
		log.Panicf("Search term must be key:value, but found %s", *searchFor)
	}

	switch search[0] {
	case "book":
		terms := []string{search[1]}
		searchBooks(kvRoot, terms)
	case "seq":
		searchSeq(kvRoot, search[1])
	default:
		log.Printf("Search %s not supported", search[0])
	}
}
