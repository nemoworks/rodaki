package main

import (
	"net/http"

	"github.com/vardius/gorouter/v4"
)

type Route struct {
	Name        string
	Method      string
	Pattern     string
	HandlerFunc http.HandlerFunc
}

func NewRouter(apihandler *APIHandler) gorouter.Router {
	router := gorouter.New()

	routes := []Route{
		{
			"ListHistoricalLocation",
			"POST",
			"/apis/ListHistoricalLocation",
			apihandler.listHistoricalLocation,
		},
		{
			"ListHistoricalLocation",
			"OPTIONS",
			"/apis/ListHistoricalLocation",
			apihandler.listHistoricalLocation,
		},
		{
			"test",
			"GET",
			"/apis/test",
			apihandler.test,
		},
	}
	for _, route := range routes {
		router.Handle(route.Method, route.Pattern, route.HandlerFunc)
	}

	return router
}
