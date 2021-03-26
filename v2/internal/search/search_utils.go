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

	"flibustier_v2/internal/consts"
	"flibustier_v2/internal/messages"
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

type searchResult struct {
	foundBooks   []messages.Book
	foundAuthors []messages.Author
	foundSeqs    []messages.Sequence
}


func MakeSearchQuery() searchQuery {
	return searchQuery{
		searchType:  "",
		searchTerms: []string{},
	}
}

func MakeSearchResult() searchResult {
	return searchResult{
		foundBooks:   []messages.Book{},
		foundAuthors: []messages.Author{},
		foundSeqs:    []messages.Sequence{},
	}
}

func searchTermPresent(query searchQuery, term string) bool {
	for _, t := range query.searchTerms {
		if t == term {
			return true
		}
	}

	return false
}

func (q searchQuery) String() string {
	return fmt.Sprintf(
		"searchQuery { searchType = %s, searchTerms = %s}",
		q.searchType,
		strings.Join(q.searchTerms, "|"))
}

func ParseSearchType(stype string) (SearchType, error) {
	switch strings.ToLower(strings.TrimSpace(stype)) {
	case "id":
		return SearchById, nil
	case "name":
		return SearchByName, nil
	default:
		return "", errors.Errorf("Unknown search type: '%s'", stype)
	}
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

func Search(kvRoot string, query searchQuery) (searchResult, error) {
	var kv *bitcask.Bitcask
	var err error
	switch query.searchFor {
	case SearchAuthor:
		kv, err = bitcask.Open(path.Join(kvRoot, consts.AUTHORS_KV))
	case SearchBook:
		kv, err = bitcask.Open(path.Join(kvRoot, consts.BOOKS_KV))
	case SearchSeq:
		kv, err = bitcask.Open(path.Join(kvRoot, consts.SEQ_KV))
	}
	defer kv.Close()
	if err != nil {
		return MakeSearchResult(), err
	}

	result := MakeSearchResult()
	kv.Scan([]byte(""), func(key []byte) error {
		if query.searchType == SearchById {
			if searchTermPresent(query, string(key)) {
				// TODO: handle this situation
			}
		} else {
			// Means we are not searching for IDs and have to iterate
			bytes, _ := kv.Get(key)
			switch query.searchFor {
			case SearchBook:
				foundMatch, msg := matchBook(bytes, query)
				if foundMatch {
					b := messages.Book{}
					proto.Merge(&b, msg)
					result.foundBooks = append(result.foundBooks, b)
				}

			case SearchAuthor:
				foundMatch, msg := matchAuthor(bytes, query)
				if foundMatch {
					a := messages.Author{}
					proto.Merge(&a, msg)
					result.foundAuthors = append(result.foundAuthors, a)
				}

			case SearchSeq:
				foundMatch, msg := matchSeq(bytes, query)
				if foundMatch {
					s := messages.Sequence{}
					proto.Merge(&s, msg)
					result.foundSeqs = append(result.foundSeqs, s)
				}

			default:
				log.Panicf("Unknown match type: %s", query.searchFor)
			}
		}

		return nil
	})

	return result, nil
}

func searchBooks(kvRoot *string, searchTerm []string) {
	booksKv, err := bitcask.Open(path.Join(*kvRoot, consts.BOOKS_KV))
	defer booksKv.Close()

	if err != nil {
		log.Panic(err)
	}

	booksKv.Scan([]byte(""), func(key []byte) error {
		bytes, _ := booksKv.Get(key)
		book := messages.Book{}
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

type extractHaystacks func(msg proto.Message) []string

func genericMatch(query searchQuery, bytes []byte, msg proto.Message, extractor extractHaystacks) (bool, proto.Message) {
	if proto.Unmarshal(bytes, msg) != nil {
		return false, msg
	}

	haystacks := extractor(msg)
	for _, needle := range query.searchTerms {
		for _, hs := range haystacks {
			match, _ := regexp.MatchString(strings.ToLower(needle), strings.ToLower(hs))
			if match {
				return true, msg
			}
		}
	}

	return false, msg
}

func matchBook(bytes []byte, query searchQuery) (bool, proto.Message) {
	return genericMatch(query, bytes, &messages.Book{}, func(msg proto.Message) []string {
		var book messages.Book
		proto.Merge(&book, msg)

		return []string{book.Title}
	})
}

func matchAuthor(bytes []byte, query searchQuery) (bool, proto.Message) {
	return genericMatch(query, bytes, &messages.Author{}, func(msg proto.Message) []string {
		var author messages.Author
		proto.Merge(&author, msg)

		return []string{author.FirstName, author.MiddleName, author.LastName}
	})
}

func matchSeq(bytes []byte, query searchQuery) (bool, proto.Message) {
	return genericMatch(query, bytes, &messages.Sequence{}, func(msg proto.Message) []string {
		var seq messages.Sequence
		proto.Merge(&seq, msg)

		return []string{seq.SequenceName}
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
		seq := messages.Sequence{}
		if proto.Unmarshal(bytes, &seq) == nil {
			match, _ := regexp.MatchString(searchTerm, strings.ToLower(seq.SequenceName))
			if match {
				log.Printf("SeqId: %s\tSeqName: %s", key, seq.SequenceName)
			}
		}

		return nil
	})
}
