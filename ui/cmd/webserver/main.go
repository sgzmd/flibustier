package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func main() {
	router := gin.Default()

	router.LoadHTMLGlob("./templates/*")
	router.Static("./assets", "./assets")

	router.GET("/", index)
	router.Run(":8080")
}

func index(c *gin.Context) {
	c.HTML(http.StatusOK, "login.html", gin.H{})
}
