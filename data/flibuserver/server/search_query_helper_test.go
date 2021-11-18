package main

import (
	"strings"
	"testing"
)

func TestCreateAuthorQuery(t *testing.T) {
	sql := CreateAuthorSearchQuery("test")
	if !strings.Contains(sql, "test*") {
		t.Fatalf("Author query is wrong, doesn't contain `test*`: %s", sql)
	}
}
