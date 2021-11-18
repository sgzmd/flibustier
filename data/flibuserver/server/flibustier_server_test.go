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
	FLIBUSTA_DB = "../testutils/flibusta-test.db"
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

func TestMain(m *testing.M) {
	// listener, err := net.Listen("tcp", ":0")
	// if err != nil {
	// 	panic(err)
	// }

	// Finding out which port are we listening on
	// port := listener.Addr().(*net.TCPAddr).Port

	// s := grpc.NewServer()
	// srv, err := NewServer(FLIBUSTA_DB)
	// if err != nil {
	// 	log.Fatalf("Couldn't create server: %v", err)
	// 	os.Exit(2)
	// }
	// defer srv.Close()
	// pb.RegisterFlibustierServer(s, srv)

	// log.Printf("Server is now running on localhost:%d", port)
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
