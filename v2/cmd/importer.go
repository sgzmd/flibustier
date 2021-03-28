package main

import (
	"flag"
	"log"
	"strings"

	_ "github.com/mattn/go-sqlite3" // Import go-sqlite3 library

	"flibustier_v2/internal/data"
	"flibustier_v2/internal/importer"
)


func main() {
	sqlitePath := flag.String("sqlite_db_path", "./flibusta.data", "Path to SQLite3 database dump")
	dbpath := flag.String("db", "", "Root of data storage directory")

	extractOnlySeq := flag.String(
		"extract_seq",
		"",
		"Extracts only these seqs, comma-separated, for testing",
	)

	checkIntegrity := flag.Bool(
		"check_integrity",
		true,
		"Verify that all authors were imported correctly for books",
	)

	flag.Parse()

	var extractSequences []string
	if *extractOnlySeq == "" {
		extractSequences = nil
	} else {
		extractSequences = strings.Split(*extractOnlySeq, ",")
	}

	db,err := data.OpenDB(*dbpath)
	if err != nil {
		log.Panicf("Couldn't open data %s: %s", dbpath, err)
	}

	importer.RunMain(sqlitePath, db, checkIntegrity, extractSequences)
}

