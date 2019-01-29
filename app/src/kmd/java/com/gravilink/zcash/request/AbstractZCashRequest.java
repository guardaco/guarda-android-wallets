package com.gravilink.zcash.request;

import android.util.Base64;
import android.util.Log;

import com.gravilink.zcash.ZCashException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

public abstract class AbstractZCashRequest {
  protected static String nodeAddress;
  protected static String auth;
  protected static String explorerAddress;
  private static long lastExplorerQuery;
  private static final Object lastExplorerQueryMutex = new Object();
  private static final long explorerTimeout = 100;

  public static void setNodeAddress(String address) {
    AbstractZCashRequest.nodeAddress = address;
  }

  public static void setAuth(String user, String password) {
    AbstractZCashRequest.auth = String.format("%s:%s", user, password);
  }

  public static void setExplorerAddress(String address) {
    AbstractZCashRequest.explorerAddress = address;
  }

  public static HttpsURLConnection getNodeConnection() throws ZCashException {
    URL url;
    try {
      url = new URL(nodeAddress);
    } catch (MalformedURLException e) {
      throw new ZCashException("Invalid URL.", e);
    }

    try {
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      String encodedAuth = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
      conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
      conn.setDoOutput(true);
      conn.setDoInput(true);
      return conn;
    } catch (IOException e) {
      throw new ZCashException("Cannot establish connection with node", e);
    }
  }

  public static JSONObject queryNode(JSONObject request) throws ZCashException {
    HttpsURLConnection conn;
    conn = getNodeConnection();
    try {
      DataOutputStream os = new DataOutputStream(conn.getOutputStream());
      os.writeBytes(request.toString());
      os.flush();
      os.close();
      conn.connect();
    } catch (IOException e) {
      throw new ZCashException("Cannot query node.", e);
    }

    return getResponseObject(conn);
  }

  public static JSONObject queryExplorer(String uri) throws ZCashException {
    return getResponseObject(queryExplorerForConnection(uri));

  }

  public static JSONArray queryExplorerForArray(String uri) throws ZCashException {
    return getResponseArray(queryExplorerForConnection(uri));
  }

  public static HttpsURLConnection queryExplorerForConnection(String uri) throws ZCashException {
    URL url;
    Log.i("URI", uri);
    try {
      url = new URL(uri);
    } catch (MalformedURLException e) {
      throw new ZCashException("Invalid explorer address.", e);
    }

    synchronized (lastExplorerQueryMutex) {
      long currentTime = System.currentTimeMillis();
      long sleepTime = explorerTimeout + lastExplorerQuery - currentTime;
      if (sleepTime > 0) {
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
      }

      lastExplorerQuery = currentTime;
      HttpsURLConnection conn;
      try {
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
      } catch (IOException e) {
        throw new ZCashException("Cannot establish connection with explorer.", e);
      }

      return conn;
    }
  }

  public static JSONObject getResponseObject(HttpURLConnection conn) throws ZCashException {
    JSONObject object;
    try {
      object = new JSONObject(getResponseString(conn));
    } catch (JSONException e) {
      throw new ZCashException("Cannot parse response from node.", e);
    }

    return object;
  }

  public static JSONArray getResponseArray(HttpURLConnection conn) throws ZCashException {
    JSONArray array;
    try {
      array = new JSONArray(getResponseString(conn));
    } catch (JSONException e) {
      throw new ZCashException("Cannot parse response from node.", e);
    }

    return array;
  }

  public static String getResponseString(HttpURLConnection conn) throws ZCashException {
    String jsonString;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }

      br.close();
      jsonString = sb.toString();
      //Log.i("RAW RESPONSE", jsonString);
      conn.disconnect();
    } catch (IOException e) {
      throw new ZCashException("Cannot get response from connection.", e);
    }

    return jsonString;
  }
}
