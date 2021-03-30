package importer

import (
	"database/sql"
	"errors"
	"fmt"
	"log"
	"os"
	"strings"

	bolt "go.etcd.io/bbolt"
	"google.golang.org/protobuf/encoding/prototext"
	"google.golang.org/protobuf/proto"

	"github.com/sgzmd/flibustier/internal/data"
	"github.com/sgzmd/flibustier/internal/messages"
)

const SQL_AUTHORS = `
select author_name.AvtorId author_id, author_name.FirstName, author_name.MiddleName,
author_name.LastName from libavtorname author_name
`

const SQL_BOOKS = `
		select book.BookId,
			   GROUP_CONCAT(author_book.AvtorId) authors_kv,
			   book.Title
		from libbook book,
			 libavtor author_book
		where book.BookId = author_book.BookId
		  and book.Deleted != '1'
		group by book.BookId
`

const SQL_SEQS = `
select seq.SeqId, sn.SeqName, GROUP_CONCAT(DISTINCT seq.BookId)
from libseq seq,
     libseqname sn,
     libbook book
where seq.SeqId = sn.SeqId
  and book.BookId = seq.BookId
  and book.Deleted != '1'
group by seq.SeqId, sn.SeqName
`

var ErrEntityExists = errors.New("Entity Exists")

func RunMain(sqlitePath *string, db *bolt.DB, checkIntegrity *bool, extractOnlySeq []string) {
	sqliteDb := openSqlite3Db(sqlitePath)
	defer sqliteDb.Close()

	extractOnlySeqs := make(map[string]bool)
	extractOnlyBooks := make(map[string]bool)
	extractOnlyAuthors := make(map[string]bool)

	filteringEnabled := false
	if extractOnlySeq != nil {
		authors, books := getAuthorsAndBooksForSequences(extractOnlySeq, sqliteDb)
		for _, author := range authors {
			extractOnlyAuthors[author] = true
		}
		for _, book := range books {
			extractOnlyBooks[book] = true
		}
		for _, seq := range extractOnlySeq {
			extractOnlySeqs[seq] = true
		}
		filteringEnabled = true
	}

	q, err := sqliteDb.Query(SQL_AUTHORS)

	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to query DB: %s", err)
		os.Exit(1)
	}

	log.Print("Stage 1: importing all authors")
	counter := 0
	db.Batch(func(tx *bolt.Tx) error {
		for q.Next() {
			var authorId int32
			var firstName, middleName, lastName string

			err = q.Scan(&authorId, &firstName, &middleName, &lastName)
			if err != nil {
				log.Panic("Failed to read record", err)
			}

			authorIdStr := fmt.Sprint(authorId)

			if filteringEnabled {
				if !extractOnlyAuthors[authorIdStr] {
					continue
				} else {
					log.Printf("Test dataset creation mode, including author %s", authorIdStr)
				}
			}

			author := messages.Author{
				FirstName:        firstName,
				MiddleName:       middleName,
				LastName:         lastName,
				FlibustaAuthorId: authorIdStr,
			}

			out, err := proto.Marshal(&author)
			if err != nil {
				log.Panicf("Couldn't serialize author %d: %s; err=%s", authorId, author, err)
			}
			key := []byte(authorIdStr)

			// Checking author for existence
			bucket := tx.Bucket(data.Authors())
			err = bucket.Put(key, out)

			if err != nil {
				log.Printf("Failed to import author %s because %s", prototext.Format(&author), err)
			}

			counter++
			if counter%1000 == 0 {
				log.Printf("%d authorsKv processed", counter)
			}
		}

		log.Printf("Imported %d authors, stage 2: importing books", counter)

		booksQuery, booksQueryErr := sqliteDb.Query(SQL_BOOKS)
		if booksQueryErr != nil {
			log.Panic("Failed to query DB", err)
		}
		defer booksQuery.Close()

		counter = 0
		for booksQuery.Next() {
			var bookId int32
			var title, authors string

			err = booksQuery.Scan(&bookId, &authors, &title)
			if err != nil {
				log.Panic("Failed to read a record", err)
			}

			authorsArr := strings.Split(authors, ",")
			bookIdStr := fmt.Sprint(bookId)

			if filteringEnabled {
				if !extractOnlyBooks[bookIdStr] {
					continue
				} else {
					log.Printf("Test dataset creation mode, including book %s", bookIdStr)
				}
			}

			book := messages.Book{
				FlibustaBookId:   bookIdStr,
				FlibustaAuthorId: authorsArr,
				Title:            title,
			}

			if *checkIntegrity {
				// Sanity checking that all authors are available in the database
				//for _, a := range authorsArr {
				//	tmpAuthor := messages.Author{}
				//	bytes, err := authorsKv.Get([]byte(a))
				//	if err != nil {
				//		log.Panicf("Couldn't get author data for author %s", a)
				//	}
				//	if proto.Unmarshal(bytes, &tmpAuthor) != nil {
				//		log.Panicf("Couldn't unmarshal data for author %s", a)
				//	}
				//}
			}

			bookBytes, err := proto.Marshal(&book)
			if err != nil {
				panic(err)
			}

			if tx.Bucket(data.Books()).Put([]byte(bookIdStr), bookBytes) != nil {
				log.Printf("Failed to write book %s because of %s", prototext.Format(&book), err)
			}

			counter++
			if counter%1000 == 0 {
				log.Printf("%d books processed", counter)
			}
		}

		log.Printf("Imported %d books, stage 3: importing sequences", counter)
		seqQuery, seqErr := sqliteDb.Query(SQL_SEQS)
		if seqErr != nil {
			log.Panic(seqErr)
		}
		defer seqQuery.Close()

		counter = 0
		for seqQuery.Next() {
			var seqId int32
			var seqName, bookIds string

			if e := seqQuery.Scan(&seqId, &seqName, &bookIds); e != nil {
				log.Panic("Failed to unpack SQL record", e)
			}

			seqIdStr := fmt.Sprint(seqId)

			if filteringEnabled {
				if !extractOnlySeqs[seqIdStr] {
					continue
				} else {
					log.Printf("Test dataset creation mode, including seq %s", seqIdStr)
				}
			}

			bookIdArr := strings.Split(bookIds, ",")
			seq := messages.Sequence{
				FlibustaSequenceId: seqIdStr,
				SequenceName:       seqName,
				BookId:             bookIdArr,
			}
			marshalled, err := proto.Marshal(&seq)
			if err != nil {
				log.Panic("Couldn't marshall proto", err)
			}

			if tx.Bucket(data.Sequences()).Put([]byte(seqIdStr), marshalled) != nil {
				log.Printf("Failed to import sequence %s because %s", prototext.Format(&seq), err)
			}
			counter++
		}

		return nil
	})

	log.Printf("Imported %d sequences, now done.", counter)
}

func openSqlite3Db(sqlitePath *string) *sql.DB {
	db, err := sql.Open("sqlite3", *sqlitePath)
	if err != nil {
		log.Panic("Couldn't open database", err)
	}
	return db
}

// Extracts book and author IDs corresponding to passed list of sequence ids
// Returns slices of author and book IDs
func getAuthorsAndBooksForSequences(sequences []string, db *sql.DB) ([]string, []string) {
	const SQL = `
		select seq.SeqId,
			   GROUP_CONCAT(DISTINCT seq.BookId)     books,
			   GROUP_CONCAT(DISTINCT author.AvtorId) authors
		from libseq seq,
			 libseqname sn,
			 libbook book,
			 libavtor author
		where seq.SeqId = sn.SeqId
		  and book.BookId = seq.BookId
		  and book.Deleted != '1'
		  and author.BookId = book.BookId
		  and seq.SeqId IN (%s)
		group by seq.SeqId, sn.SeqName;
	`

	ids := strings.Join(sequences, ",")
	q, err := db.Query(fmt.Sprintf(SQL, ids))
	if err != nil {
		log.Panic(err)
	}

	var authorsResult []string
	var booksResult []string
	for q.Next() {
		var seqId int
		var books, authors string

		q.Scan(&seqId, &books, &authors)
		log.Printf("seqId=%d books=%s authors=%s", seqId, books, authors)

		authorsResult = append(authorsResult, strings.Split(authors, ",")...)
		booksResult = append(booksResult, strings.Split(books, ",")...)
	}

	return authorsResult, booksResult
}
