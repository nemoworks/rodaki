package main

import (
	"flag"
	"log"
	"net/http"
)

var (
	host     string
	port     string
	user     string
	password string
)

func main() {
	flag.StringVar(&host, "host", "127.0.0.1", "--host=127.0.0.1")
	flag.StringVar(&port, "port", "6667", "--port=6667")
	flag.StringVar(&user, "user", "root", "--user=root")
	flag.StringVar(&password, "password", "root", "--password=root")
	flag.Parse()
	apiHandler := NewAPIHandler()
	router := NewRouter(apiHandler)
	log.Fatal(http.ListenAndServe(":10086", router))
}
