package follow

import (
	"errors"
	"fmt"
	"log"
	"path"

	"github.com/prologic/bitcask"
	"google.golang.org/protobuf/encoding/prototext"
	"google.golang.org/protobuf/proto"

	"github.com/sgzmd/flibustier/internal/consts"
	"github.com/sgzmd/flibustier/internal/messages"
	"github.com/sgzmd/flibustier/internal/search"
)

func FollowAuthor(kvroot string, author messages.Author) error {
	kv, err := bitcask.Open(path.Join(kvroot, consts.FOLLOW_KV))
	if err != nil {
		log.Panic(err)
	}

	key := makeAuthorFollowKey(author)
	keybytes, _ := proto.Marshal(&key)
	_, err = kv.Get(keybytes)
	if err == nil {
		// We are already following this author, nothing to do
		msg := fmt.Sprintf("Already following author %s\n", prototext.Format(&author))
		return errors.New(msg)
	} else if err != bitcask.ErrKeyNotFound {
		// There was some error with lookup, we shouldn't proceed
		log.Panic(err)
	}
	// Things are OK, let's proceed.
	obj := messages.FollowObject{
		Key:  &key,
		Book: nil,
	}

	reqstr := fmt.Sprintf("%s:%s", consts.SearchPrefixAuthor, author.FlibustaAuthorId)
	log.Printf("Issuing search request %s", reqstr)
	req, err := search.ParseQuery(reqstr, search.SearchById)
	if err != nil {
		return err
	}

	result, err := search.Search(kvroot, req)
	if err != nil {
		return err
	}

	if len(result.FoundBooks) == 0 {
		return errors.New(fmt.Sprintf("No books found for author %s", prototext.Format(&author)))
	}

	obj.Book = make([]*messages.Book, len(result.FoundBooks))
	for idx, book := range result.FoundBooks {
		obj.Book[idx] = &book
	}

	objbytes, err := proto.Marshal(&obj)
	if err != nil {
		return err
	}

	kv.Put(keybytes, objbytes)

	return nil
}

func makeAuthorFollowKey(author messages.Author) messages.FollowKey {
	return messages.FollowKey{
		FlibustaId: author.FlibustaAuthorId,
		FollowType: consts.SearchPrefixAuthor,
	}
}
