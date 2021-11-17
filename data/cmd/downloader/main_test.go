package main

import "testing"

func TestCreateUrlList(t *testing.T) {
	const BASE_URL = "http://flibusta.site/sql/"
	urls := CreateUrlList(BASE_URL)
	if len(urls) < 4 {
		t.Fatalf("Too few URLs obtained, something is wrong")
	}
}
