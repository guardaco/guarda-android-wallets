package com.bitshares.bitshareswallet.wallet;

import android.content.SharedPreferences;
import android.text.TextUtils;

//import com.bitshares.bitshareswallet.BitsharesApplication;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.constants.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by lorne on 07/09/2017.
 */

public class FullNodeServerSelect {
    private List<String> mListNode = Arrays.asList(
            "wss://bts.guarda.co/wss",
            "wss://bts.guarda.co/wss"
//            "wss://bitshares.openledger.info/ws",
//            "wss://eu.openledger.info/ws",
//            "wss://bit.btsabc.org/ws",
//            "wss://bts.transwiser.com/ws",
//            "wss://bitshares.dacplay.org/ws",
//            "wss://bitshares-api.wancloud.io/ws",
//            "wss://openledger.hk/ws",
//            "wss://secure.freedomledger.com/ws",
//            "wss://dexnode.net/ws",
//            "wss://altcap.io/ws",
//            "wss://bitshares.crypto.fans/ws"
    );

    public String getServer() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GuardaApp.context_s);
//        String strServer = sharedPreferences.getString("full_node_api_server", "autoselect");
//        if (strServer.equals("autoselect")) {
//            return getAutoSelectServer();
//        } else {
//            return strServer;
//        }
        return Common.NODE_ADDRESS;
    }

    private String getAutoSelectServer() {
        List<WebSocket> listWebsocket = new ArrayList<>();
        final Object objectSync = new Object();

        final int nTotalCount = mListNode.size();
        final List<String> listSelectedServer = new ArrayList<>();
        for (final String strServer : mListNode) {
            Request request = new Request.Builder().url(strServer).build();
            OkHttpClient okHttpClient = new OkHttpClient();
            WebSocket webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(""); // 失败，则填空

                        if (listSelectedServer.size() == nTotalCount) {
                            objectSync.notify();;
                        }
                    }
                }

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    super.onOpen(webSocket, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(strServer);
                        objectSync.notify();
                    }
                }
            });
            listWebsocket.add(webSocket);
        }

        String strResultServer = "";
        synchronized (objectSync) {
            if (listSelectedServer.isEmpty() == false && listSelectedServer.size() < nTotalCount ) {
                for (String strServer : listSelectedServer) {
                    if (strServer.isEmpty() == false) {
                        strResultServer = strServer;
                        break;
                    }
                }
            }

            if (strResultServer.isEmpty()) {
                try {
                    objectSync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (listSelectedServer.isEmpty() == false && listSelectedServer.size() < nTotalCount ) {
                    for (String strServer : listSelectedServer) {
                        if (strServer.isEmpty() == false) {
                            strResultServer = strServer;
                            break;
                        }
                    }
                }
            }
        }

        for (WebSocket webSocket : listWebsocket) {
            webSocket.close(1000, "close");
        }

        return strResultServer;
    }
}
