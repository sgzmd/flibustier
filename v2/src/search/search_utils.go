package search

import (
	"fmt"
	"log"
	"path"
	"regexp"
	"strings"

	"github.com/pkg/errors"
	"github.com/prologic/bitcask"
	"google.golang.org/protobuf/proto"

	"flibustier_v2/src/com.sigizmund/flibustier"
	"flibustier_v2/src/consts"
)

type SearchType string
type SearchFor string

const (
	SearchById   SearchType = "SearchById"
	SearchByName SearchType = "SearchByName"

	SearchAuthor SearchFor = "SearchAuthor"
	SearchBook   SearchFor = "SearchBook"
	SearchSeq    SearchFor = "SearchSeq"
)

const searchPrefixAuthor = "author"
const searchPrefixBook = "book"
const searchPrefixSeq = "seq"

// Not exported
type searchQuery struct {
	searchType SearchType
	searchFor  SearchFor

	searchTerms []string
}

func MakeSearchQuery() searchQuery {
	return searchQuery{
		searchType:  "",
		searchTerms: []string{},
	}
}

func (q searchQuery) String() string {
	return fmt.Sprintf(
		"searchQuery { searchType = %s, searchTerms = %s}",
		q.searchType,
		strings.Join(q.searchTerms, "|"))
}

// Parses search term into a SearchQuery
// Term should look like auth:
func ParseQuery(term string, termType SearchType) (searchQuery, error) {
	if len(term) < 3 {
		return MakeSearchQuery(), errors.Errorf("Query term '%s' is too short", term)
	}
	args := strings.Split(term, ":")
	if len(args) != 2 {
		return MakeSearchQuery(), errors.Errorf("'%s' is not a valid search term", term)
	}

	searchFor := args[0]
	searchObjects := strings.Split(args[1], ",")

	for idx, obj := range searchObjects {
		searchObjects[idx] = strings.ToLower(strings.TrimSpace(obj))
	}

	query := MakeSearchQuery()
	query.searchType = termType
	query.searchTerms = searchObjects

	switch searchFor {
	case searchPrefixAuthor:
		query.searchFor = SearchAuthor
	case searchPrefixBook:
		query.searchFor = SearchBook
	case searchPrefixSeq:
		query.searchFor = SearchSeq
	default:
		return MakeSearchQuery(), errors.Errorf("Search prefix '%s' is not supported", searchFor)
	}

	return query, nil
}

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
