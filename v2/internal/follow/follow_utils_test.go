package follow

import (
	"io/ioutil"
	"os"
	"path"
	"testing"

	"github.com/prologic/bitcask"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"

	"github.com/sgzmd/flibustier/internal/consts"
	"github.com/sgzmd/flibustier/internal/messages"
	"github.com/sgzmd/flibustier/internal/search"
	"github.com/sgzmd/flibustier/internal/testutils"
)

func TestFollowAuthor(t *testing.T) {
	author := getTestAuthor()
	tempkvdir, err := ioutil.TempDir(os.TempDir(), "kvfollow")
	assert.Nil(t, err)
	defer os.RemoveAll(tempkvdir)

	assert.NotNil(t, FollowAuthor(tempkvdir, author))

	kv, err := bitcask.Open(path.Join(tempkvdir, consts.FOLLOW_KV))
	assert.Nil(t, err)

	found := false
	kv.Scan([]byte(""), func(key []byte) error {
		bytes, err := kv.Get(key)
		assert.Nil(t, err)

		obj := messages.FollowObject{}
		assert.Nil(t, proto.Unmarshal(bytes, &obj))

		assert.GreaterOrEqual(t, len(obj.Book), 4)
		found = true

		return nil
	})

	assert.True(t, found)
}

func getTestAuthor() messages.Author {
	sr, err := search.ParseQuery("author:метельский", search.SearchByName)
	if err != nil {
		panic(err)
	}
	result, err := search.Search(testutils.KvRoot, sr)
	if err != nil {
		panic(err)
	}

	return result.FoundAuthors[0]
}
