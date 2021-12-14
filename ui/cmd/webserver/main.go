package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"sigizmund.com/flibustier/proto"
)

func main() {
	router := gin.Default()

	p := &proto.ListTrackedEntriesRequest{}
	p.UserId = "1"

	router.LoadHTMLGlob("./templates/*")
	router.Static("./assets", "./assets")

	router.GET("/", index)
	router.Run(":8080")
}

func index(c *gin.Context) {
	c.HTML(http.StatusOK, "login.html", gin.H{})
}
