package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"net/http"
	"sync"

	"github.com/apache/iotdb-client-go/client"
	"github.com/apache/iotdb-client-go/rpc"
)

type APIHandler struct {
	client []*client.Session
	//申请一个锁
	mutex *sync.Mutex
}

var vehicleLocationSql = "select location from root.highway.Vehicle.%s where timestamp >= %s"
var vehicleAllLocationSql = "select location from root.highway.Vehicle.%s"
var sessions []*client.Session
var timeout int64 = 5000
var sessionnum = 200
var currentUse = 0

func NewAPIHandler() *APIHandler {

	config := &client.Config{
		Host:      host,
		Port:      port,
		UserName:  user,
		Password:  password,
		FetchSize: 1021 * 1024 * 2,
	}
	for i := 0; i < sessionnum; i++ {
		session := client.NewSession(config)
		if err := session.Open(false, 0); err != nil {
			log.Fatal(err)
			continue
		}
		sessions = append(sessions, session)
	}

	return &APIHandler{sessions, &sync.Mutex{}}
}
func (apiHandler *APIHandler) listHistoricalLocation(w http.ResponseWriter, r *http.Request) {
	//fmt.Println(r.GetBody())
	if r.Method == "OPTIONS" {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-AllowHeaders", "Accept, Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization")
		w.Header().Set("Content-Type", "appli-cation/json")
		return
	}
	id := r.PostFormValue("id")
	//starttime := r.PostFormValue("startTime")

	fmt.Println(id)
	fmt.Println(host)
	sql := fmt.Sprintf(vehicleAllLocationSql, id)
	fmt.Println(sql)
	var useSession = 0
	apiHandler.mutex.Lock()
	useSession = currentUse
	currentUse = (currentUse + 1) % len(apiHandler.client)
	apiHandler.mutex.Unlock()
	sessionDataSet, err := apiHandler.client[useSession].ExecuteQueryStatement(sql, timeout)
	if err == nil {
		apiHandler.responseJSON(extractLocationDataSet(sessionDataSet), w, http.StatusOK)
		sessionDataSet.Close()
	} else {
		log.Println(err)
		apiHandler.responseJSON(err, w, http.StatusNotFound)
	}
}
func (apiHandler *APIHandler) test(w http.ResponseWriter, req *http.Request) {
	fmt.Fprintf(w, "<h1>static file server</h1><p><a href='./static'>folder</p></a>")

	ip, port, err := net.SplitHostPort(req.RemoteAddr)
	if err != nil {
		//return nil, fmt.Errorf("userip: %q is not IP:port", req.RemoteAddr)

		fmt.Fprintf(w, "userip: %q is not IP:port", req.RemoteAddr)
	}

	userIP := net.ParseIP(ip)
	if userIP == nil {
		//return nil, fmt.Errorf("userip: %q is not IP:port", req.RemoteAddr)
		fmt.Fprintf(w, "userip: %q is not IP:port", req.RemoteAddr)
		return
	}

	// This will only be defined when site is accessed via non-anonymous proxy
	// and takes precedence over RemoteAddr
	// Header.Get is case-insensitive
	forward := req.Header.Get("X-Forwarded-For")

	fmt.Fprintf(w, "<p>IP: %s</p>", ip)
	fmt.Fprintf(w, "<p>Port: %s</p>", port)
	fmt.Fprintf(w, "<p>Forwarded for: %s</p>", forward)
	fmt.Fprintf(w, "sss")
}
func checkError(status *rpc.TSStatus, err error) {
	if err != nil {
		log.Fatal(err)
	}

	if status != nil {
		if err = client.VerifySuccess(status); err != nil {
			log.Println(err)
		}
	}
}
func (apiHandler *APIHandler) responseJSON(body interface{}, w http.ResponseWriter, statusCode int) {
	jsonResponse, err := json.Marshal(body)

	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Headers", "Accept, Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization")
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	w.Write(jsonResponse)
}

func extractLocationDataSet(sds *client.SessionDataSet) []interface{} {
	showTimestamp := !sds.IsIgnoreTimeStamp()
	var results []interface{} = []interface{}{}
	if showTimestamp {
		fmt.Print("Time\t\t\t\t")
	}

	for i := 0; i < sds.GetColumnCount(); i++ {
		fmt.Printf("%s\t", sds.GetColumnName(i))
	}
	fmt.Println()

	for next, err := sds.Next(); err == nil && next; next, err = sds.Next() {
		if showTimestamp {
			fmt.Printf("%s\t", sds.GetText(client.TimestampColumnName))
		}
		for i := 0; i < sds.GetColumnCount(); i++ {
			columnName := sds.GetColumnName(i)
			v := sds.GetValue(columnName)
			if v == nil {
				v = "null"
			}
			fmt.Printf("%v\t\t", v)
			if i == 0 {
				results = append(results, []interface{}{sds.GetText(client.TimestampColumnName), v})
			}
		}
		fmt.Println()

	}
	return results
}
