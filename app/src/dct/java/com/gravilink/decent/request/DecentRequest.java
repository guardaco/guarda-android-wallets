package com.gravilink.decent.request;

import com.gravilink.decent.DecentException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DecentRequest {

  private static final int DATABASE_ID = 0;
  private static final int HISTORY_ID = 1;
  private static final int BROADCAST_ID = 2;

  private static final int LOGIN = 0;
  private static final int API = 1;
  private static final int WORK = 2;

  private WebSocket webSocket;
  private AbstractDecentRequest request;
  private int databaseApiIndex = -1, historyApiIndex = -1, broadcastApiIndex = -1;
  private int currentStage;

  public DecentRequest(WebSocketFactory wsfactory, String endpoint, final AbstractDecentRequest request) {
    this.request = request;
    try {
      webSocket = wsfactory.createSocket(endpoint);
    } catch (IOException e) {
      request.onError(e);
    }
    webSocket.addListener(new WebSocketAdapter() {

      @Override
      public void onTextMessage(WebSocket websocket, String text) throws Exception {
        //System.err.println("<<<" + text);
        //System.err.flush();
        DecentRequest.this.onResponse(text);
      }

      @Override
      public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //System.err.println(">>>" + frame.getPayloadText());
        //System.err.flush();
      }

      @Override
      public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        request.onError(cause);
        websocket.disconnect();
      }

      @Override
      public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        DecentRequest.this.onConnected();
      }

      @Override
      public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                 boolean closedByServer) throws Exception {
        request.manager.finishRequest();
      }

    });

    if (request.isBroadcastRequired() || request.isDatabaseRequired() || request.isHistoryRequired()) {
      currentStage = LOGIN;
    } else {
      currentStage = WORK;
    }

    webSocket.connectAsynchronously();
  }

  private void onConnected() {
    switch (currentStage) {
      case LOGIN: {
        webSocket.sendText("{\"id\":0,\"method\":\"call\",\"params\":[1,\"login\",[\"\",\"\"]]}");
        webSocket.flush();
        break;
      }

      case WORK: {
        if (request.onReady(webSocket, -1, -1, -1)) {
          webSocket.disconnect();
        }
        break;
      }
    }
  }

  private void onResponse(String response) {
    try {
      JSONObject jsonData = new JSONObject(response);

      int id = jsonData.getInt("id");
      if (!jsonData.isNull("error")) {
        request.onError(new DecentException(jsonData.getString("error")));
        webSocket.disconnect();
        return;
      }

      switch (currentStage) {
        case LOGIN: {
          if (request.isDatabaseRequired())
            webSocket.sendText(String.format(Locale.ENGLISH, "{\"id\":%d,\"method\":\"call\",\"params\":[1,\"database\",[\"\",\"\"]]}", DATABASE_ID));
          if (request.isHistoryRequired())
            webSocket.sendText(String.format(Locale.ENGLISH, "{\"id\":%d,\"method\":\"call\",\"params\":[1,\"history\",[\"\",\"\"]]}", HISTORY_ID));
          if (request.isBroadcastRequired())
            webSocket.sendText(String.format(Locale.ENGLISH, "{\"id\":%d,\"method\":\"call\",\"params\":[1,\"network_broadcast\",[\"\",\"\"]]}", BROADCAST_ID));
          webSocket.flush();

          currentStage++;
          break;
        }

        case API: {
          if (id == DATABASE_ID)
            databaseApiIndex = jsonData.getInt("result");
          if (id == HISTORY_ID)
            historyApiIndex = jsonData.getInt("result");
          if (id == BROADCAST_ID)
            broadcastApiIndex = jsonData.getInt("result");

          if ((!request.isDatabaseRequired() || databaseApiIndex != -1) && (!request.isHistoryRequired() || historyApiIndex != -1) && (!request.isBroadcastRequired() || broadcastApiIndex != -1)) {
            currentStage++;
            if (request.onReady(webSocket, databaseApiIndex, historyApiIndex, broadcastApiIndex)) {
              webSocket.disconnect();
            }
          }
          break;
        }

        case WORK: {
          if (request.onResponse(webSocket, jsonData, databaseApiIndex, historyApiIndex, broadcastApiIndex)) {
            webSocket.disconnect();
          }
          break;
        }
      }
    } catch (JSONException e) {
      request.onError(e);
      webSocket.disconnect();
    }
  }

}
