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

	"google.golang.org/grpc/reflection"

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

func (s *server) SearchAuthors(req *pb.SearchRequest) ([]*pb.FoundEntry, error) {
	log.Printf("Searching for author: %s", req)
	sql := CreateAuthorSearchQuery(req.SearchTerm)

	rows, err := s.Database.Query(sql)

	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var entries []*pb.FoundEntry = make([]*pb.FoundEntry, 0, 10)
	for rows.Next() {
		var authorName string
		var authorId int64
		var count int32

		err = rows.Scan(&authorName, &authorId, &count)
		if err != nil {
			log.Fatalf("Failed to scan the row: %v", err)
			return nil, err
		}

		entries = append(entries, &pb.FoundEntry{
			EntryType:   pb.EntryType_AUTHOR,
			Author:      authorName,
			EntryName:   authorName,
			EntryId:     authorId,
			NumEntities: count,
		})
	}

	return entries, nil
}

func (s *server) SearchSeries(req *pb.SearchRequest) ([]*pb.FoundEntry, error) {
	log.Printf("Searching for series: %s", req)
	sql := CreateSequenceSearchQuery(req.SearchTerm)
	rows, err := s.Database.Query(sql)

	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var entries []*pb.FoundEntry = make([]*pb.FoundEntry, 0, 10)
	for rows.Next() {
		var seqName string
		var authors string
		var seqId int64
		var count int32

		err = rows.Scan(&seqName, &authors, &seqId, &count)
		if err != nil {
			log.Fatalf("Failed to scan the row: %v", err)
			return nil, err
		}

		entries = append(entries, &pb.FoundEntry{
			EntryType:   pb.EntryType_SERIES,
			Author:      authors,
			EntryName:   seqName,
			EntryId:     seqId,
			NumEntities: count,
		})
	}

	return entries, nil
}

func (s *server) GlobalSearch(ctx context.Context, in *pb.SearchRequest) (*pb.SearchResponse, error) {
	log.Printf("Received: %v", in.GetSearchTerm())

	var entries []*pb.FoundEntry = make([]*pb.FoundEntry, 0, 10)

	// If there's no filter for series
	if in.EntryTypeFilter != pb.EntryType_SERIES {
		authors, err := s.SearchAuthors(in)
		if err != nil {
			return nil, err
		}
		entries = append(entries, authors...)
	}

	if in.EntryTypeFilter != pb.EntryType_AUTHOR {
		series, err := s.SearchSeries(in)
		if err != nil {
			return nil, err
		}
		entries = append(entries, series...)
	}

	return &pb.SearchResponse{
		OriginalRequest: in,
		Entry:           entries,
	}, nil
}

// In case I forgot what am I doing here:
// Creating a curried function which takes a slice of books
// and returns a comparator to be used with this slice for sort.SliceStable
func CreateBookComparator(books []*pb.Book) func(int, int) bool {
	return func(i, j int) bool {
		return books[i].BookId < books[j].BookId
	}
}

func (s *server) CheckUpdates(ctx context.Context, in *pb.UpdateCheckRequest) (*pb.UpdateCheckResponse, error) {
	log.Printf("Received: %v", in)

	response := make([]*pb.UpdateRequired, 0)

	astm, err := s.Database.Prepare(`
		select b.BookId, b.Title from libbook b, libavtor a 
		where b.BookId = a.BookId and a.AvtorId = ?`)

	if err != nil {
		return nil, err
	}

	// We will start with a very naive and simple implementation
	for _, entry := range in.TrackedEntry {
		if entry.EntryType == pb.EntryType_AUTHOR {
			rs, err := astm.Query(entry.EntryId)
			if err != nil {
				return nil, err
			}

			new_books := make([]*pb.Book, 0)

			for rs.Next() {
				var bookId int32
				var title string
				rs.Scan(&bookId, &title)

				new_books = append(new_books, &pb.Book{BookName: title, BookId: bookId})
			}

			if entry.NumEntries != int32(len(new_books)) {
				// If it is equal, no updates required

				// sort.SliceStable(entry.Book, CreateBookComparator(entry.Book))
				// sort.SliceStable(new_books, CreateBookComparator(new_books))
				old_book_map := make(map[int]*pb.Book)
				for _, b := range entry.Book {
					old_book_map[int(b.BookId)] = b
				}

				newly_added_books := make([]*pb.Book, 0, len(new_books)-int(entry.NumEntries))
				for _, b := range new_books {
					_, exists := old_book_map[int(b.BookId)]
					if !exists {
						// Well we found the missing book
						newly_added_books = append(newly_added_books, b)
					}
				}

				if len(newly_added_books) > 0 {
					response = append(response, &pb.UpdateRequired{
						TrackedEntry:  entry,
						NewNumEntries: int32(len(new_books)),
						NewBook:       newly_added_books,
					})
				}
			}
		}
	}

	return &pb.UpdateCheckResponse{UpdateRequired: response}, nil
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
	reflection.Register(s)
	log.Printf("server listening at %v", lis.Addr())

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
