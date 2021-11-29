package main

import (
	"context"
	pb "flibustaimporter/flibuserver/proto"
	"log"
	"net"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"
)

const (
	FLIBUSTA_DB = "../../../testutils/flibusta-test.db"
)

var (
	client pb.FlibustierClient = nil
)

func dialer() func(context.Context, string) (net.Conn, error) {
	listener := bufconn.Listen(1024 * 1024)

	server := grpc.NewServer()

	srv, err := NewServer(FLIBUSTA_DB)
	if err != nil {
		panic(err)
	}
	pb.RegisterFlibustierServer(server, srv)

	go func() {
		if err := server.Serve(listener); err != nil {
			log.Fatal(err)
		}
	}()

	return func(context.Context, string) (net.Conn, error) {
		return listener.Dial()
	}
}

func TestSmokeTest(t *testing.T) {
	const TERM = "метел"
	result, err := client.GlobalSearch(context.Background(), &pb.SearchRequest{SearchTerm: TERM})
	if err != nil {
		log.Fatalf("Failed the smoke test: %v", err)
		t.Fatalf("Smoke test failed")
	} else {
		assert.Equal(t, TERM, result.OriginalRequest.SearchTerm, "Request doesn't look right")
	}
}

func TestSearchEverything(t *testing.T) {
	const TERM = "Николай Александрович Метельский"
	result, err := client.GlobalSearch(context.Background(), &pb.SearchRequest{SearchTerm: TERM})
	assert.Nil(t, err)
	assert.Len(t, result.Entry, 2)
	assert.Equal(t, result.Entry[0].Author, "Николай Александрович Метельский")
	assert.Equal(t, result.Entry[1].EntryName, "Унесенный ветром")
}

func TestSearchAuthor(t *testing.T) {
	const TERM = "Метельский"
	result, err := client.GlobalSearch(context.Background(),
		&pb.SearchRequest{SearchTerm: TERM, EntryTypeFilter: pb.EntryType_AUTHOR})
	assert.Nil(t, err)
	assert.Len(t, result.Entry, 1)
	assert.Equal(t, result.Entry[0].Author, "Николай Александрович Метельский")
}

func TestSearchSeries(t *testing.T) {
	// note that searching for author of the series
	const TERM = "Метельский"
	result, err := client.GlobalSearch(context.Background(),
		&pb.SearchRequest{SearchTerm: TERM, EntryTypeFilter: pb.EntryType_SERIES})
	assert.Nil(t, err)
	assert.Len(t, result.Entry, 1)
	assert.Equal(t, result.Entry[0].EntryName, "Унесенный ветром")
}

func TestCheckUpdates_Author(t *testing.T) {
	books := []*pb.Book{{BookId: 452501, BookName: "Чужие маски"}}

	tracked := &pb.TrackedEntry{
		EntryType:  pb.EntryType_AUTHOR,
		EntryName:  "Метельский",
		EntryId:    109170,
		NumEntries: 1,
		UserId:     "123",
		Book:       books,
	}

	request := pb.UpdateCheckRequest{
		TrackedEntry: []*pb.TrackedEntry{tracked},
	}

	resp, err := client.CheckUpdates(context.Background(), &request)
	if err != nil {
		t.Fatalf("Failed: %v", err)
	} else {
		// t.Errorf("Result: %s", resp.String())
		if len(resp.UpdateRequired) != 1 || resp.UpdateRequired[0].NewNumEntries != 9 {
			t.Fatalf(
				"Expect to have 1 UpdateRequired entity with 9 new_num_entries, but have: %s",
				resp)
		}
	}
}

func TestCheckUpdates_Series(t *testing.T) {
	books := []*pb.Book{{BookId: 452501, BookName: "Чужие маски"}}

	tracked := &pb.TrackedEntry{
		EntryType:  pb.EntryType_SERIES,
		EntryName:  "Унесенный ветром",
		EntryId:    34145,
		NumEntries: 1,
		UserId:     "123",
		Book:       books,
	}

	request := pb.UpdateCheckRequest{
		TrackedEntry: []*pb.TrackedEntry{tracked},
	}

	resp, err := client.CheckUpdates(context.Background(), &request)
	if err != nil {
		t.Fatalf("Failed: %v", err)
	} else {
		// t.Errorf("Result: %s", resp.String())
		if len(resp.UpdateRequired) != 1 || resp.UpdateRequired[0].NewNumEntries != 9 {
			t.Fatalf(
				"Expect to have 1 UpdateRequired entity with 9 new_num_entries, but have: %s",
				resp)
		}
	}
}

func TestServer_GetSeriesBooks(t *testing.T) {
	req := &pb.SequenceBooksRequest{SequenceId: 34145}
	resp, err := client.GetSeriesBooks(context.Background(), req)
	if err != nil {
		t.Fatalf("Failed: %+v", err)
	} else {
		assert.Equal(t, req.SequenceId, resp.EntityId)
		assert.Len(t, resp.Book, 8)

		assert.Equal(t, "Унесенный ветром: Меняя маски. Теряя маски. Чужие маски", resp.Book[0].BookName)
		assert.Equal(t, "Унесенный ветром", resp.EntityName.GetSequenceName())
	}
}

func TestServer_GetAuthorBooks(t *testing.T) {
	req := &pb.AuthorBooksRequest{AuthorId: 109170}
	resp, err := client.GetAuthorBooks(context.Background(), req)
	if err != nil {
		t.Fatalf("Failed: %+v", err)
	} else {
		assert.Equal(t, req.AuthorId, resp.EntityId)
		assert.Len(t, resp.Book, 8)

		assert.Equal(t, "Чужие маски", resp.Book[0].BookName)
		assert.Equal(t, &pb.AuthorName{
			LastName:   "Метельский",
			MiddleName: "Александрович",
			FirstName:  "Николай"}, resp.EntityName.GetAuthorName())
	}
}

func TestMain(m *testing.M) {
	ctx := context.Background()
	// Creating a client
	conn, err := grpc.DialContext(ctx, "", grpc.WithInsecure(), grpc.WithContextDialer(dialer()))
	if err != nil {
		panic(err)
	}
	defer conn.Close()
	client = pb.NewFlibustierClient(conn)

	ret := m.Run()

	os.Exit(ret)
}
