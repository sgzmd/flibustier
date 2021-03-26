package search

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

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
