package main

import (
	"context"
	pb "flibustier_proto"
	"log"
	"net"

	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"
)

func dialer(flibustaDb string) func(context.Context, string) (net.Conn, error) {
	listener := bufconn.Listen(1024 * 1024)

	server := grpc.NewServer()

	srv, err := NewServer(flibustaDb, "" /* in memory datastore */)
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
