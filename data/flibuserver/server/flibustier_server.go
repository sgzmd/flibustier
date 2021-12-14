package main

import (
	"context"
	"database/sql"
	"flag"
	"fmt"
	"log"
	"net"
	"os"
	"sync"
	"time"

	pb "flibustier_proto"

	badger "github.com/dgraph-io/badger/v3"
	"github.com/golang/protobuf/proto"

	"google.golang.org/grpc/reflection"

	_ "github.com/mattn/go-sqlite3"

	"google.golang.org/grpc"
)

type server struct {
	pb.UnimplementedFlibustierServer
	sqliteDb *sql.DB
	data     *badger.DB
	Lock     sync.RWMutex
}

var (
	port       = flag.Int("port", 9000, "RPC server port")
	flibustaDb = flag.String("flibusta_db", "", "Path to Flibusta SQLite3 database")
	datastore  = flag.String("datastore", "", "Path to the data store to use")
)

func (s *server) SearchAuthors(req *pb.SearchRequest) ([]*pb.FoundEntry, error) {
	log.Printf("Searching for author: %s", req)

	s.Lock.RLock()
	defer s.Lock.RUnlock()

	sql := CreateAuthorSearchQuery(req.SearchTerm)

	rows, err := s.sqliteDb.Query(sql)

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

	s.Lock.RLock()
	defer s.Lock.RUnlock()

	sql := CreateSequenceSearchQuery(req.SearchTerm)
	rows, err := s.sqliteDb.Query(sql)

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

	s.Lock.RLock()
	defer s.Lock.RUnlock()

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

// CheckUpdates Searches for updates in the collection of tracked entries.
// Implementation is very straightforward and not very performant
// but it's possible that it's good enough.
// See: ../proto/flibustier.proto for proto definitions.
func (s *server) CheckUpdates(ctx context.Context, in *pb.UpdateCheckRequest) (*pb.UpdateCheckResponse, error) {
	log.Printf("Received: %v", in)

	s.Lock.RLock()
	defer s.Lock.RUnlock()

	response := make([]*pb.UpdateRequired, 0)

	astm, err := s.sqliteDb.Prepare(`
		select b.BookId, b.Title from libbook b, libavtor a 
		where b.BookId = a.BookId and a.AvtorId = ?`)

	if err != nil {
		return nil, err
	}

	sstm, err := s.sqliteDb.Prepare(`
	select b.BookId, b.Title from libbook b, libseq s 
	where s.BookId = b.BookId and s.SeqId = ?
	`)
	if err != nil {
		return nil, err
	}

	// We will start with a very naive and simple implementation
	for _, entry := range in.TrackedEntry {
		var rs *sql.Rows
		var err error

		var statement *sql.Stmt
		if entry.EntryType == pb.EntryType_AUTHOR {
			statement = astm
		} else if entry.EntryType == pb.EntryType_SERIES {
			statement = sstm
		}

		rs, err = statement.Query(entry.EntryId)
		if err != nil {
			return nil, err
		}

		if !rs.Next() {
			return nil,
				fmt.Errorf("exceptional situation: nothing was found for %v with EntryId %d",
					statement,
					entry.EntryId)
		}

		newBooks := make([]*pb.Book, 0)

		for rs.Next() {
			var bookId int32
			var title string
			rs.Scan(&bookId, &title)

			newBooks = append(newBooks, &pb.Book{BookName: title, BookId: bookId})
		}

		if entry.NumEntries != int32(len(newBooks)) {
			// If it is equal, no updates required

			// sort.SliceStable(entry.Book, CreateBookComparator(entry.Book))
			// sort.SliceStable(new_books, CreateBookComparator(new_books))
			oldBookMap := make(map[int]*pb.Book)
			for _, b := range entry.Book {
				oldBookMap[int(b.BookId)] = b
			}

			newlyAddedBooks := make([]*pb.Book, 0, len(newBooks)-int(entry.NumEntries))
			for _, b := range newBooks {
				_, exists := oldBookMap[int(b.BookId)]
				if !exists {
					// Well we found the missing book
					newlyAddedBooks = append(newlyAddedBooks, b)
				}
			}

			if len(newlyAddedBooks) > 0 {
				response = append(response, &pb.UpdateRequired{
					TrackedEntry:  entry,
					NewNumEntries: int32(len(newBooks)),
					NewBook:       newlyAddedBooks,
				})
			}
		}
	}

	return &pb.UpdateCheckResponse{UpdateRequired: response}, nil
}

func GetEntityBooks(sql *sql.Stmt, entityId int32) ([]*pb.Book, error) {
	rs, err := sql.Query(entityId)

	if err != nil {
		return nil, err
	}

	books := make([]*pb.Book, 0)
	for rs.Next() {
		var bookTitle string
		var bookId int32

		rs.Scan(&bookTitle, &bookId)
		books = append(books, &pb.Book{BookId: bookId, BookName: bookTitle})
	}

	return books, nil
}

func (s *server) GetAuthorBooks(ctx context.Context, in *pb.AuthorBooksRequest) (*pb.EntityBookResponse, error) {
	log.Printf("GetAuthorBooks: %+v", in)

	s.Lock.RLock()
	defer s.Lock.RUnlock()

	sql, err := s.sqliteDb.Prepare(`
		select 
		  lb.Title,
		  lb.Bookid
		from libbook lb, libavtor la, author_fts a
		where la.BookId = lb.BookId 
		and a.authorId = la.AvtorId
		and lb.Deleted != '1'
		and la.AvtorId = ?
		group by la.BookId order by la.BookId;`)

	if err != nil {
		return nil, err
	}
	books, err := GetEntityBooks(sql, in.AuthorId)

	if err != nil {
		return nil, err
	}

	sql, err = s.sqliteDb.Prepare(`
		select an.FirstName, an.MiddleName, an.LastName 
		from libavtorname an
		where an.AvtorId = ?`)
	if err != nil {
		return nil, err
	}
	rs, err := sql.Query(in.AuthorId)
	if err != nil {
		return nil, err
	}

	if rs.Next() {
		var firstName, middleName, lastName string
		rs.Scan(&firstName, &middleName, &lastName)
		name := &pb.EntityName{Name: &pb.EntityName_AuthorName{
			AuthorName: &pb.AuthorName{
				FirstName:  firstName,
				MiddleName: middleName,
				LastName:   lastName}}}

		return &pb.EntityBookResponse{Book: books, EntityId: in.AuthorId, EntityName: name}, nil
	}

	return nil, fmt.Errorf("no author associated with id %d", in.AuthorId)
}

func (s *server) GetSeriesBooks(ctx context.Context, in *pb.SequenceBooksRequest) (*pb.EntityBookResponse, error) {
	log.Printf("GetSeriesBooks: %+v", in)

	s.Lock.RLock()
	defer s.Lock.RUnlock()

	sql, err := s.sqliteDb.Prepare(`
		SELECT b.Title, b.BookId
		FROM libseq ls, libseqname lsn , libbook b
		WHERE ls.seqId = lsn.seqId and ls.seqId = ? and ls.BookId = b.BookId and b.Deleted != '1'
				  group by b.BookId
				  order by ls.SeqNumb;`)

	if err != nil {
		return nil, err
	}
	books, err := GetEntityBooks(sql, in.SequenceId)

	if err != nil {
		return nil, err
	}

	rs, err := s.sqliteDb.Query("select SeqName from libseqname where SeqId = ?", in.SequenceId)
	if err != nil {
		return nil, err
	}
	if rs.Next() {
		var seqName string
		rs.Scan(&seqName)
		name := &pb.EntityName{Name: &pb.EntityName_SequenceName{SequenceName: seqName}}

		return &pb.EntityBookResponse{Book: books, EntityId: in.SequenceId, EntityName: name}, nil
	}

	return nil, fmt.Errorf("no series associated with id %d", in.SequenceId)
}

func (s *server) TrackEntry(ctx context.Context, entry *pb.TrackedEntry) (*pb.TrackEntryResponse, error) {
	log.Printf("TrackEntry: %+v", entry)
	key := pb.TrackedEntryKey{EntityType: entry.EntryType, EntityId: entry.EntryId, UserId: entry.UserId}
	alreadyTracked := false
	err := s.data.View(func(txn *badger.Txn) error {
		it := txn.NewIterator(badger.DefaultIteratorOptions)
		defer it.Close()
		prefix, err := proto.Marshal(&key)
		if err != nil {
			return err
		}

		for it.Seek(prefix); it.ValidForPrefix(prefix); it.Next() {
			alreadyTracked = true
			return nil
		}

		return nil
	})
	if err != nil {
		return nil, err
	}

	if alreadyTracked {
		return &pb.TrackEntryResponse{Key: &key, Result: pb.TrackEntryResult_TRACK_ALREADY_TRACKED}, nil
	}

	s.data.Update(func(txn *badger.Txn) error {
		key, err := proto.Marshal(&key)
		if err != nil {
			return err
		}

		value, err := proto.Marshal(entry)
		if err != nil {
			return err
		}

		return txn.Set(key, value)
	})

	if err != nil {
		return nil, err
	}

	return &pb.TrackEntryResponse{Key: &key, Result: pb.TrackEntryResult_TRACK_OK}, nil
}

func (s *server) ListTrackedEntries(ctx context.Context, req *pb.ListTrackedEntriesRequest) (*pb.ListTrackedEntriesResponse, error) {
	log.Printf("ListTrackedEntries: %+v", req)
	entries := make([]*pb.TrackedEntry, 0)
	err := s.data.View(func(txn *badger.Txn) error {
		it := txn.NewIterator(badger.DefaultIteratorOptions)
		defer it.Close()

		for it.Rewind(); it.Valid(); it.Next() {
			marshalledValue := []byte{}
			key := &pb.TrackedEntryKey{}

			err := proto.Unmarshal(it.Item().Key(), key)
			if err != nil {
				return err
			}

			// This isn't really efficient, we should use prefix scan
			if key.UserId != req.UserId {
				continue
			}

			err = it.Item().Value(func(val []byte) error {
				marshalledValue = val
				return nil
			})
			if err != nil {
				return err
			}

			trackedEntry := pb.TrackedEntry{}
			err = proto.Unmarshal(marshalledValue, &trackedEntry)
			if err != nil {
				return err
			}
			entries = append(entries, &trackedEntry)
		}

		return nil
	})

	if err != nil {
		return nil, err
	}

	return &pb.ListTrackedEntriesResponse{Entry: entries}, nil
}

func (s *server) UntrackEntry(ctx context.Context, req *pb.TrackedEntryKey) (*pb.UntrackEntryResponse, error) {
	log.Printf("UntrackEntry: %+v", req)
	err := s.data.Update(func(txn *badger.Txn) error {
		key, err := proto.Marshal(req)
		if err != nil {
			return nil
		}

		return txn.Delete(key)
	})
	if err != nil {
		return nil, err
	} else {
		return &pb.UntrackEntryResponse{Key: req, Result: pb.UntrackEntryResult_UNTRACK_OK}, nil
	}
}

func (s *server) Close() {
	log.Println("Closing database connection.")
	s.sqliteDb.Close()
}

func OpenDatabase(db_path string) (*sql.DB, error) {
	return sql.Open("sqlite3", db_path)
}

func NewServer(db_path string, datastore string) (*server, error) {
	srv := new(server)

	db, err := OpenDatabase(db_path)
	if err != nil {
		return nil, err
	}
	srv.sqliteDb = db

	var opt badger.Options
	if datastore == "" {
		opt = badger.DefaultOptions("").WithInMemory(true)
	} else {
		opt = badger.DefaultOptions(datastore)
	}

	srv.data, err = badger.Open(opt)
	if err != nil {
		return nil, err
	}

	return srv, nil
}

func (s *server) Shutdown() {
	s.sqliteDb.Close()
	s.data.Close()
}

func main() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s := grpc.NewServer()
	srv, err := NewServer(*flibustaDb, *datastore)
	if err != nil {
		log.Fatalf("Couldn't create server: %v", err)
		os.Exit(2)
	}
	defer srv.Close()

	pb.RegisterFlibustierServer(s, srv)
	reflection.Register(s)
	log.Printf("server listening at %v", lis.Addr())

	ticker := time.NewTicker(10 * time.Minute)
	go func() {
		for range ticker.C {
			log.Printf("Re-opening database ...")
			srv.Lock.Lock()
			db, err := OpenDatabase(*flibustaDb)
			srv.Lock.Unlock()
			if err != nil {
				log.Fatalf("Failed to open database: %s", err)
				os.Exit(1)
			}

			srv.sqliteDb = db
			log.Printf("Database re-opened.")
		}
	}()

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
