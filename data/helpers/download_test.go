package helpers

import (
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"testing"
)

const FILE_NAME = "local-file"

type Server struct {
	Port int
}

var (
	server *Server
)

func (server *Server) Start() {
	http.HandleFunc("/hi", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "result")
	})
	httpServer := &http.Server{}
	listener, err := net.ListenTCP("tcp4", &net.TCPAddr{IP: net.IPv4(127, 0, 0, 1)})

	if err != nil {
		log.Fatal("error creating listener")
	}

	server.Port = listener.Addr().(*net.TCPAddr).Port

	go httpServer.Serve(listener)
}

func TestDownloadFile(t *testing.T) {
	e := DownloadFile(FILE_NAME, fmt.Sprintf("http://localhost:%d/hi", server.Port))
	if e != nil {
		t.Fatalf("Failed because of %s", e)
	}

	data, err := os.ReadFile(FILE_NAME)
	if err != nil {
		t.Fatalf("Failed to re-read file: %s", err)
	}
	if string(data) != "result" {
		t.Fatalf("Expected 'result' but got %s", string(data))
	}
}

func TestMain(m *testing.M) {
	server = &Server{}
	server.Start()

	ret := m.Run()

	os.Remove(FILE_NAME)
	os.Exit(ret)
}
