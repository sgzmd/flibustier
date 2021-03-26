package main

import (
	"log"
	"path"
	"regexp"
	"strings"

	"github.com/prologic/bitcask"
	"google.golang.org/protobuf/proto"

	"flibustier_v2/src/com.sigizmund/flibustier"
	"flibustier_v2/src/consts"
)

func searchBooks(kvRoot *string, searchTerm []string) {
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
			for _, term := range searchTerm {
				match, _ := regexp.MatchString(term, strings.ToLower(book.Title))
				if match {
					log.Printf("Found a matching book: %s", book.Title)
				}
			}
		}

		return nil
	})
}

func searchSeq(kvRoot *string, searchTerm string) {
	seqKv, err := bitcask.Open(path.Join(*kvRoot, consts.SEQ_KV))
	defer seqKv.Close()

	if err != nil {
		log.Panic(err)
	}

	seqKv.Scan([]byte(""), func(key []byte) error {
		bytes, _ := seqKv.Get(key)
		seq := flibustier.Sequence{}
		if proto.Unmarshal(bytes, &seq) == nil {
			match, _ := regexp.MatchString(searchTerm, strings.ToLower(seq.SequenceName))
			if match {
				log.Printf("SeqId: %s\tSeqName: %s", key, seq.SequenceName)
			}
		}

		return nil
	})
}

