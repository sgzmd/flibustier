package search

import (
	"fmt"
	"log"
	"os"
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

// Not exported
type searchQuery struct {
	searchType SearchType
	searchFor  SearchFor

	searchTerms []string
}

type searchResult struct {
	FoundBooks   []messages.Book
	FoundAuthors []messages.Author
	FoundSeqs    []messages.Sequence
}

func MakeSearchQuery() searchQuery {
	return searchQuery{
		searchType:  "",
		searchTerms: []string{},
	}
}

func MakeSearchResult() searchResult {
	return searchResult{
		FoundBooks:   []messages.Book{},
		FoundAuthors: []messages.Author{},
		FoundSeqs:    []messages.Sequence{},
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
	case consts.SearchPrefixAuthor:
		query.searchFor = SearchAuthor
	case consts.SearchPrefixBook:
		query.searchFor = SearchBook
	case consts.SearchPrefixSeq:
		query.searchFor = SearchSeq
	default:
		return MakeSearchQuery(), errors.Errorf("Search prefix '%s' is not supported", searchFor)
	}

	return query, nil
}

func Search(kvRoot string, query searchQuery) (searchResult, error) {
	switch query.searchFor {
	case SearchAuthor:
		return searchAuthor(kvRoot, query)
	case SearchBook:
		return searchBook(kvRoot, query)
	case SearchSeq:
		return searchSeq(kvRoot, query)
	default:
		return MakeSearchResult(), errors.Errorf("Unsupported searchFor=%s", query.searchFor)
	}
}

func searchSeq(kvRoot string, query searchQuery) (searchResult, error) {
	result := MakeSearchResult()

	kvpath := path.Join(kvRoot, consts.SEQ_KV)
	if _, err := os.Stat(kvpath); os.IsNotExist(err) {
		log.Panicf("KV file %s doesn't exist", kvpath)
	}

	kv, err := bitcask.Open(kvpath)
	defer kv.Close()
	if err != nil {
		return result, err
	}

	kv.Scan([]byte(""), func(key []byte) error {
		seq := messages.Sequence{}
		bytes, err := kv.Get(key)
		if err != nil {
			return err
		}
		proto.Unmarshal(bytes, &seq)
		if query.searchType == SearchById {
			if searchTermPresent(query, string(key)) {
				result.FoundSeqs = append(result.FoundSeqs, seq)
			}
		} else {
			foundMatch, msg := matchSeq(bytes, query)
			if foundMatch {
				s := messages.Sequence{}
				proto.Merge(&s, msg)
				result.FoundSeqs = append(result.FoundSeqs, s)
			}
		}

		return nil
	})

	return result, nil
}

func searchBook(kvRoot string, query searchQuery) (searchResult, error) {
	result := MakeSearchResult()

	kv, err := bitcask.Open(path.Join(kvRoot, consts.BOOKS_KV))
	defer kv.Close()
	if err != nil {
		return result, err
	}

	kv.Scan([]byte(""), func(key []byte) error {
		book := messages.Book{}
		bytes, err := kv.Get(key)
		if err != nil {
			return err
		}
		proto.Unmarshal(bytes, &book)
		if query.searchType == SearchById {
			if searchTermPresent(query, string(key)) {
				result.FoundBooks = append(result.FoundBooks, book)
			}
		} else {
			foundMatch, msg := matchBook(bytes, query)
			if foundMatch {
				b := messages.Book{}
				proto.Merge(&b, msg)
				result.FoundBooks = append(result.FoundBooks, b)
			}
		}

		return nil
	})

	return result, nil
}

func searchAuthor(kvRoot string, query searchQuery) (searchResult, error) {
	result := MakeSearchResult()

	kv, err := bitcask.Open(path.Join(kvRoot, consts.AUTHORS_KV))
	defer kv.Close()
	if err != nil {
		return result, err
	}

	kv.Scan([]byte(""), func(key []byte) error {
		author := messages.Author{}
		bytes, err := kv.Get(key)
		if err != nil {
			return err
		}
		proto.Unmarshal(bytes, &author)
		if query.searchType == SearchById {
			if searchTermPresent(query, string(key)) {
				result.FoundAuthors = append(result.FoundAuthors, author)
			}
		} else {
			foundMatch, msg := matchAuthor(bytes, query)
			if foundMatch {
				a := messages.Author{}
				proto.Merge(&a, msg)
				result.FoundAuthors = append(result.FoundAuthors, a)
			}
		}

		return nil
	})

	return result, nil
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
