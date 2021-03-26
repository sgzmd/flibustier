package main

import (
	"flag"
	"fmt"

	"flibustier_v2/src/search"
)

/*

./follow -what author:<author_id>
or
./follow -what seq:<seq_id>
or
./follow -search seq:<seq_name>

 */

func main() {
	kvRoot := flag.String("kv_root", "./kv", "Root of data storage directory")
	what := flag.String("what", "", "What to follow, key:value, e.g. author:123")

	flag.Parse()

	search.searchBooks(kvRoot, []string{*what})

	fmt.Printf("root=%s, what=%s", *kvRoot, *what)
}