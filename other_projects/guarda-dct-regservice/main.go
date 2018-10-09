package main

import (
  "fmt"
  "math/rand"
  "time"
  "flag"
  "runtime"
  "log"
  "net"
  "net/http"
  "os"
  "os/signal"
  "syscall"
  "encoding/json"
  "io/ioutil"
  
  "github.com/gorilla/websocket"
)



const cliClientWebsocketAddress = "ws://127.0.0.1:8092/"
const registrarAccount = "1.2.9436";//"1.2.2537";



func main() {
  fmt.Println("call main.main()...")

  rand.Seed(time.Now().UnixNano())

  cfg := processFlags()
  if cfg.GoMaxProcs != 0 {
    runtime.GOMAXPROCS(cfg.GoMaxProcs)
  }

  if err := run(cfg); err != nil {
    log.Printf("Error in main(): %v", err)
  }

  fmt.Println("call main.main()... done!")
}



type config struct {
  ListenSpec string
  GoMaxProcs int
}



func processFlags() *config {
  cfg := &config{}
  flag.StringVar(&cfg.ListenSpec, "listen", "localhost:8080", "HTTP listen spec")
  flag.IntVar(&cfg.GoMaxProcs, "gomaxprocs", 0, "GOMAXPROCS, 0==defalut")

  flag.Parse()
  return cfg
}



func run(cfg *config) error {
  log.Printf("Starting, HTTP on: %s\n", cfg.ListenSpec)

  l, err := net.Listen("tcp", cfg.ListenSpec)
  if err != nil {
    log.Printf("Error creating listener: %v\n", err)
    return err
  }

  Start(l)

  waitForSignal()

  return nil
}



func routeUrls() {
  http.Handle("/", indexHandler())
  http.Handle("/api/register/", registerHandler())
}



func Start(listener net.Listener) {

  server := &http.Server{}
  server.ReadTimeout = 60 * time.Second
  server.WriteTimeout = 60 * time.Second
  server.MaxHeaderBytes = 1 << 16

  routeUrls()

  go server.Serve(listener)
}



func waitForSignal() {
  ch := make(chan os.Signal)
  signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
  s := <-ch
  log.Printf("Got signal: %v, exiting.", s)
}



func indexHandler() http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "guarda-dct-regservice index text")
  })
}



func registerHandler() http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    
    body, _ := ioutil.ReadAll(r.Body)
    reqDict := make(map[string]interface{})
    json.Unmarshal(body, &reqDict)
    
    c, _, err := websocket.DefaultDialer.Dial(cliClientWebsocketAddress, nil)
    if err != nil {
      fmt.Fprintf(w, "{\"status\":\"error\", \"description\":\"%s\"}", err.Error())
      return
    }
    defer c.Close()
    ansMap := make(map[string]interface{})
    logText := ""
    
    c.WriteMessage(websocket.TextMessage, []byte("{\"jsonrpc\":\"2.0\", \"id\": 1, \"method\":\"unlock\", \"params\":[\"111\"]}"))
    message, err := readWebsocketWithTimout(c)
    if err != nil {
      fmt.Fprintf(w, "{\"status\":\"error\", \"description\":\"%s\"}", err.Error())
      return
    }
    logText += fmt.Sprintf("msg received: %s\n", message)
    
    c.WriteMessage(websocket.TextMessage, []byte("{\"jsonrpc\":\"2.0\", \"id\": 2, \"method\":\"is_locked\", \"params\":[]}"))
    message, err = readWebsocketWithTimout(c)
    if err != nil {
      fmt.Fprintf(w, "{\"status\":\"error\", \"description\":\"%s\"}", err.Error())
      return
    }
    logText += fmt.Sprintf("msg received: %s\n", message)

    newName := (reqDict["newName"]).(string)
    newPublicKey := (reqDict["newPublicKey"]).(string)
    c.WriteMessage(websocket.TextMessage, []byte("{\"jsonrpc\":\"2.0\", \"id\": 2, \"method\":\"register_account\", \"params\":[\""+newName+"\", \""+newPublicKey+"\", \""+newPublicKey+"\", \""+registrarAccount+"\", true]}"))
    message, err = readWebsocketWithTimout(c)
    if err != nil {
      fmt.Fprintf(w, "{\"status\":\"error\", \"description\":\"%s\"}", err.Error())
      return
    }
    logText += fmt.Sprintf("msg received: %s\n", message)
    jsonRespDict := make(map[string]interface{})
    json.Unmarshal([]byte(message), &jsonRespDict)
    if val, ok := jsonRespDict["error"]; ok {
      ansMap["status"] = "error"
      ansMap["description"] = val
      ansMap["newName"] = newName
      ansMap["newPublicKey"] = newPublicKey
    } else {
      ansMap["status"] = "ok"
      ansMap["description"] = ""
      ansMap["newName"] = newName
      ansMap["newPublicKey"] = newPublicKey
    }
    
    _ = logText
    
    respString, _ := json.Marshal(ansMap)
    fmt.Fprintf(w, string(respString))
  })
}



func readWebsocketWithTimout(c *websocket.Conn) (string, error) {
  c.SetReadDeadline(time.Now().Add(1000*time.Millisecond))
  _, message, err := c.ReadMessage()
    return string(message), err
}
