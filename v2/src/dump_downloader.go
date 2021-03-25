package main

import (
	"database/sql"
	"flibustier_v2/src/com.sigizmund/flibustier"
	"fmt"
	_ "github.com/mattn/go-sqlite3" // Import go-sqlite3 library
	"github.com/prologic/bitcask"
	"google.golang.org/protobuf/proto"
	"log"
	"os"
	"strings"
)

func main() {
	fmt.Printf("Hello world!\n")

	db, err := sql.Open("sqlite3", "./flibusta.db")
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to open database: %s", err)
		os.Exit(1)
	}
	defer db.Close()

	q, err := db.Query(
		"select author_name.AvtorId author_id, author_name.FirstName, author_name.MiddleName, " +
			"author_name.LastName from libavtorname author_name")

	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to query DB: %s", err)
		os.Exit(1)
	}

	authors_kv, _ := bitcask.Open("./authors_kv.db")
	defer authors_kv.Close()

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
			log.Printf("Couldn't serialize author %d: %s", authorId, author)
			panic(err)
		}
		if authorIdStr == "18891" {
			fmt.Print("------------------ found ---------------")
		}
		key := []byte(authorIdStr)

		_, err = authors_kv.Get(key)
		if err != nil {
			authors_kv.Put(key, out)
		}

		counter++
		if counter%100 == 0 {
			log.Printf("%d authors_kv processed", counter)
		}
	}

	sql := `
		select book.BookId,
			   GROUP_CONCAT(author_book.AvtorId) authors_kv,
			   book.Title
		from libbook book,
			 libavtor author_book
		where book.BookId = author_book.BookId
		  and book.Deleted != '1'
		group by book.BookId`

	booksQuery, booksQueryErr := db.Query(sql)
	if booksQueryErr != nil {
		log.Panic("Failed to query DB", err)
	}
	defer booksQuery.Close()

	booksKv, _ := bitcask.Open("./booksKv.db")
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
				bytes, err := authors_kv.Get([]byte(a))
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
		if counter%100 == 0 {
			log.Printf("%d books processed", counter)
		}
	}
}
