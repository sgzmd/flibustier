package main

import (
	"flag"
	"log"
	"path"
	"regexp"
	"strings"

	"github.com/prologic/bitcask"
	"google.golang.org/protobuf/proto"

	"flibustier_v2/src/com.sigizmund/flibustier"
	"flibustier_v2/src/consts"
)

func main() {
	kvRoot := flag.String("kv_root", "kv", "Root of KV store")
	searchFor := flag.String("search", "book:.+дозор.+", "What are we searching for")


	search := strings.Split(*searchFor, ":")
	if len(search) != 2 {
		log.Panicf("Search term must be key:value, but found %s", *searchFor)
	}

	switch search[0] {
	case "book":
		searchBooks(kvRoot, search)
	default:
		log.Printf("Search %s not supported", search[0])
	}
}

func searchBooks(kvRoot *string, search []string) {
	booksKv, err := bitcask.Open(path.Join(*kvRoot, consts.BOOKS_KV))
	defer booksKv.Close()

	if err != nil {
		log.Panic(err)
	}

	booksKv.Scan([]byte(""), func(key []byte) error {
		bytes, _ := booksKv.Get(key)
		book := flibustier.Book{}
		if proto.Unmarshal(bytes, &book) == nil {
			// Parsed OK
			match, _ := regexp.MatchString(search[1], book.Title)
			if match {
				log.Printf("Found a matching book: %s", book.Title)
			}
		}

		return nil
	})
}
