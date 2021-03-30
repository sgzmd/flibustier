package search

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"

	"github.com/sgzmd/flibustier/internal/messages"
	"github.com/sgzmd/flibustier/internal/testutils"
)

// ./importer -extract_seq 34145,432,34360 -kv_root=./test-kv
func TestMakeSearchQuery(t *testing.T) {
	q := MakeSearchQuery()

	assert.NotNil(t, q.searchTerms, "searchTerms must not be nil")
}

func TestParseQuery(t *testing.T) {
	q := "author:SomeAuthor,SomeOtherAuthor"
	query, err := ParseQuery(q, SearchByName)
	assert.Nil(t, err)
	assert.Equal(t, []string{"someauthor", "someotherauthor"}, query.searchTerms)
	assert.Equal(t, SearchByName, query.searchType)
	assert.Equal(t, SearchAuthor, query.searchFor)
}

func TestParseQueryIds(t *testing.T) {
	q := "seq:123,243,431"
	query, err := ParseQuery(q, SearchById)
	assert.Nil(t, err)
	assert.Equal(t, []string{"123", "243", "431"}, query.searchTerms)
	assert.Equal(t, SearchById, query.searchType)
	assert.Equal(t, SearchSeq, query.searchFor)
}

func TestMatchBook(t *testing.T) {
	book := messages.Book{
		FlibustaBookId:   "whatever",
		FlibustaAuthorId: nil,
		Title:            "20000 leagues under the sea",
	}
	bytes, _ := proto.Marshal(&book)
	sq := MakeSearchQuery()
	sq.searchTerms = []string{"leag.+"}
	b, msg := matchBook(bytes, sq)
	assert.True(t, b, "Must match")
	assert.True(t, proto.Equal(&book, msg))

	sq.searchTerms = []string{"leg"}
	b, msg = matchBook(bytes, sq)
	assert.False(t, b, "Must not match")
	assert.True(t, proto.Equal(&book, msg))
}

func TestSearchSeq(t *testing.T) {
	q := MakeSearchQuery()
	q.searchFor = SearchSeq
	q.searchType = SearchByName
	q.searchTerms = []string{"маск"}

	result, err := Search(testutils.KvRoot, q)
	assert.Nil(t, err)
	assert.NotNil(t, result)
	assert.Equal(t, "34145", result.FoundSeqs[0].FlibustaSequenceId)
}

func TestSearchAuth(t *testing.T) {
	q := MakeSearchQuery()
	q.searchFor = SearchAuthor
	q.searchType = SearchByName
	q.searchTerms = []string{"метель"}

	result, err := Search(testutils.KvRoot, q)
	assert.Nil(t, err)
	assert.NotNil(t, result)
	assert.Equal(t, "109170", result.FoundAuthors[0].FlibustaAuthorId)
}

func TestSearchById(t *testing.T) {
	q := MakeSearchQuery()
	q.searchFor = SearchBook
	q.searchType = SearchById
	q.searchTerms = []string{"526369"}

	result, err := Search(testutils.KvRoot, q)
	assert.Nil(t, err)
	assert.Equal(t, "Запах Сумрака", result.FoundBooks[0].Title)
}
