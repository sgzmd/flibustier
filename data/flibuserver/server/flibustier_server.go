package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"net"

	pb "flibustaimporter/flibuserver/proto"

	"google.golang.org/grpc"
)

type server struct {
	pb.UnimplementedFlibustierServer
}

var (
	port = flag.Int("port", 9000, "RPC server port")
)

func (s *server) GlobalSearch(ctx context.Context, in *pb.SearchRequest) (*pb.SearchResponse, error) {
	log.Printf("Received: %v", in.GetSearchTerm())
	return &pb.SearchResponse{
		Result: "Hello " + in.GetSearchTerm(),
	}, nil
}

func main() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterFlibustierServer(s, &server{})
	log.Printf("server listening at %v", lis.Addr())

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
