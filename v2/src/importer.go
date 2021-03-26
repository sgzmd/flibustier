package main

import (
	"database/sql"
	"flag"
	"fmt"
	"log"
	"os"
	"path"
	"strings"

	_ "github.com/mattn/go-sqlite3" // Import go-sqlite3 library
	"github.com/prologic/bitcask"
	"google.golang.org/protobuf/proto"

	"flibustier_v2/src/com.sigizmund/flibustier"
	"flibustier_v2/src/consts"
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

func main() {
	sqlitePath := flag.String("sqlite_db_path", "./flibusta.db", "Path to SQLite3 database dump")
	kvRoot := flag.String("kv_root", "./kv", "Root of data storage directory")
	checkIntegrity := flag.Bool(
		"check_integrity",
		true,
		"Verify that all authors were imported correctly for books")

	flag.Parse()

	run_main(sqlitePath, kvRoot, checkIntegrity)
}

func run_main(sqlitePath *string, kvRoot *string, checkIntegrity *bool) {
	db, err := sql.Open("sqlite3", *sqlitePath)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to open database: %s", err)
		os.Exit(1)
	}
	defer db.Close()

	q, err := db.Query(SQL_AUTHORS)

	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to query DB: %s", err)
		os.Exit(1)
	}

	authorsKvPath := path.Join(*kvRoot, consts.AUTHORS_KV)
	authorsKv, _ := bitcask.Open(authorsKvPath)
	defer authorsKv.Close()

	log.Print("Stage 1: importing all authors")
	counter := 0
	for q.Next() {
		var authorId int32
		var firstName, middleName, lastName string

		err = q.Scan(&authorId, &firstName, &middleName, &lastName)
		if err != nil {
			log.Panic("Failed to read record", err)
		}

		authorIdStr := fmt.Sprint(authorId)
		author := flibustier.Author{
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

		_, err = authorsKv.Get(key)
		if err != nil {
			authorsKv.Put(key, out)
		}

		counter++
		if counter%1000 == 0 {
			log.Printf("%d authorsKv processed", counter)
		}
	}

	log.Printf("Imported %d authors, stage 2: importing books", counter)

	booksQuery, booksQueryErr := db.Query(SQL_BOOKS)
	if booksQueryErr != nil {
		log.Panic("Failed to query DB", err)
	}
	defer booksQuery.Close()

	booksKvPath := path.Join(*kvRoot, consts.BOOKS_KV)
	booksKv, _ := bitcask.Open(booksKvPath)
	defer booksKv.Close()

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

		_, err = booksKv.Get([]byte(bookIdStr))
		if err != nil {
			book := flibustier.Book{
				FlibustaBookId:   bookIdStr,
				FlibustaAuthorId: authorsArr,
				Title:            title,
			}

			// Sanity checking that all authors are available in the database
			for _, a := range authorsArr {
				tmpAuthor := flibustier.Author{}
				bytes, err := authorsKv.Get([]byte(a))
				if err != nil {
					log.Panicf("Couldn't get author data for author %s", a)
				}
				if proto.Unmarshal(bytes, &tmpAuthor) != nil {
					log.Panicf("Couldn't unmarshal data for author %s", a)
				}
			}

			bookBytes, err := proto.Marshal(&book)
			if err != nil {
				panic(err)
			}
			booksKv.Put([]byte(bookIdStr), bookBytes)
		}

		counter++
		if counter%1000 == 0 {
			log.Printf("%d books processed", counter)
		}
	}

	log.Printf("Imported %d books, stage 3: importing sequences", counter)
	seqQuery, seqErr := db.Query(SQL_SEQS)
	if seqErr != nil {
		log.Panic(seqErr)
	}
	defer seqQuery.Close()

	seqKvPath := path.Join(*kvRoot, consts.SEQ_KV)
	seqKv, err := bitcask.Open(seqKvPath)
	if err != nil {
		log.Panic(err)
	}
	defer seqKv.Close()

	counter = 0
	for seqQuery.Next() {
		var seqId int32
		var seqName, bookIds string

		if e := seqQuery.Scan(&seqId, &seqName, &bookIds); e != nil {
			log.Panic("Failed to unpack SQL record", e)
		}

		seqIdStr := fmt.Sprint(seqId)
		bookIdArr := strings.Split(bookIds, ",")
		seq := flibustier.Sequence{
			FlibustaSequenceId: seqIdStr,
			SequenceName:       seqName,
			BookId:             bookIdArr,
		}
		marshalled,err := proto.Marshal(&seq)
		if err != nil {
			log.Panic("Couldn't marshall proto", err)
		}
		seqKv.Put([]byte(seqIdStr), marshalled)
		counter++
	}

	log.Printf("Imported %d sequences, now done.", counter)
}
