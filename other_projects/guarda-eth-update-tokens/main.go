package main

import (
  "fmt"
  "flag"
  "io/ioutil"
  "encoding/json"
  "time"
  "net/http"
  "strconv"
)



var remote_tokens_url = "https://api.ethplorer.io/getTop?apiKey=freekey"



func main() {
  fmt.Println("\n\n\n-----------------------")
  fmt.Println(time.Now().String())
  fmt.Println("guarda-eth-update-tokens main()...")
  
  filePath := "tokens.json"
  checkOnly := true
  flag.StringVar(&filePath, "filePath", "tokens.json", "local tokens.json file path, it will be updated")
  flag.BoolVar(&checkOnly, "checkOnly", true, "if true, don't touch local tokens.json file")
  flag.Parse()
  
  fmt.Printf("localTokenJsonFilePath: %s\n", filePath)
  fmt.Printf("checkOnly: %t\n", checkOnly)
  
  tokensString, errRead := ioutil.ReadFile(filePath)
  if errRead != nil {
    fmt.Printf("error: unable to read file %v\n", filePath)
    fmt.Println(errRead)
    return
  }
  tokensList := make([]interface{}, 0, 0)
  errUnmarshal := json.Unmarshal(tokensString, &tokensList)
  if errUnmarshal != nil {
    fmt.Printf("error: unable to parse json\n")
    fmt.Println(errUnmarshal)
    return
  }
  fmt.Printf("tokensList.length: %d\n", len(tokensList))
  
  tokensMap := make(map[string]interface{})
  for _, val := range tokensList {
    valMap := val.(map[string]interface{})
    name := valMap["name"].(string)
    tokensMap[name] = valMap
  }
  fmt.Printf("tokensMap.length: %d\n", len(tokensMap))
  
  remoteTokensResp, err := http.Get(remote_tokens_url)
  if err != nil {
    fmt.Printf("error: unable to download file %v\n", remote_tokens_url)
    fmt.Println(err)
    return
  }
  defer remoteTokensResp.Body.Close()
  
  remoteTokensBody, err := ioutil.ReadAll(remoteTokensResp.Body)
  if err != nil {
    fmt.Printf("error: unable to read file %v\n", remote_tokens_url)
    fmt.Println(err)
    return
  }

  remoteTokensJson := make(map[string]interface{})
  errUnmarshal = json.Unmarshal(remoteTokensBody, &remoteTokensJson)
  if errUnmarshal != nil {
    fmt.Printf("error: unable to parse remote json\n")
    fmt.Println(errUnmarshal)
    return
  }
  remoteTokensList := remoteTokensJson["tokens"].([]interface{})
  fmt.Printf("remoteTokensList.length: %d\n", len(remoteTokensList))

  newTokensCount := 0
  for _, val := range remoteTokensList {
    valMap := val.(map[string]interface{})
    name := valMap["symbol"].(string)
    if _, ok := tokensMap[name]; !ok {
      decimal := anyToInt64(valMap["decimals"])
      address := anyToString(valMap["address"])
      newTokenMap := make(map[string]interface{})
      newTokenMap["name"] = name
      newTokenMap["decimal"] = decimal
      newTokenMap["id"] = ""
      newTokenMap["jsonrpc"] = "2.0"
      newTokenMap["method"] = "eth_call"
      newTokenParamsMap := make(map[string]interface{})
      newTokenParamsMap["to"] = address
      newTokenParamsMap["data"] = ""
      newTokenMap["params"] = [2]interface{}{newTokenParamsMap, "pending"}
      fmt.Printf("new token: name=%s, decimal=%d, address=%s\n", name, decimal, address)
      tokensList = append(tokensList, newTokenMap)
      newTokensCount += 1
    } 
  }
  fmt.Printf("newTokensCount: %d\n", newTokensCount)
  
  if newTokensCount <= 0 {
    fmt.Printf("no new tokens found. exit\n")
    return
  }
  
  if checkOnly {
    fmt.Printf("checkOnly is true, skip saving file\n")
  } else {
    tokensBytes, err := json.MarshalIndent(tokensList, "", "    ")
    if err != nil {
      fmt.Printf("error: unable to marshal json\n")
      fmt.Println(err)
      return
    }
    err = ioutil.WriteFile(filePath, tokensBytes, 0644)
    if err != nil {
      fmt.Printf("error: unable to save file %s\n", filePath)
      fmt.Println(err)
      return
    }
    fmt.Printf("save file %s ... done!\n", filePath)
  }
}



func anyToString(unk interface{}) string {
  switch unk.(type) {
  case string:
    return unk.(string)
  case float64:
    return fmt.Sprintf("%.0f", unk.(float64))
  default:
    return "unknown_type"
  }
}



func anyToInt64(unk interface{}) int64 {
  switch unk.(type) {
  case string:
    i64, err := strconv.ParseInt(unk.(string), 10, 64)
    if err == nil {
      return i64
    } else {
      return 0
    }
  case float64:
    return anyToInt64(fmt.Sprintf("%.0f", unk.(float64)))
  case int64:
    return unk.(int64)
  default:
    return 0
  }
}
