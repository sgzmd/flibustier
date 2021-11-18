package main

import (
	"context"
	"database/sql"
	"flag"
	"fmt"
	"log"
	"net"
	"os"

	pb "flibustaimporter/flibuserver/proto"

	_ "github.com/mattn/go-sqlite3"

	"google.golang.org/grpc"
)

type server struct {
	pb.UnimplementedFlibustierServer
	Database *sql.DB
}

var (
	port        = flag.Int("port", 9000, "RPC server port")
	flibusta_db = flag.String("flibusta_db", "", "Path to Flibusta SQLite3 database")
)

func (s *server) GlobalSearch(ctx context.Context, in *pb.SearchRequest) (*pb.SearchResponse, error) {
	log.Printf("Received: %v", in.GetSearchTerm())
	return &pb.SearchResponse{OriginalRequest: in}, nil
}

func (s *server) Close() {
	log.Println("Closing database connection.")
	s.Database.Close()
}

func NewServer(db_path string) (*server, error) {
	srv := new(server)

	db, err := sql.Open("sqlite3", db_path)
	if err != nil {
		return nil, err
	}
	srv.Database = db

	return srv, nil
}

func main() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s := grpc.NewServer()
	srv, err := NewServer(*flibusta_db)
	if err != nil {
		log.Fatalf("Couldn't create server: %v", err)
		os.Exit(2)
	}
	defer srv.Close()

	pb.RegisterFlibustierServer(s, srv)
	log.Printf("server listening at %v", lis.Addr())

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
