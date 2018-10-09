package com.bitshares.bitshareswallet.wallet;

import android.text.TextUtils;
import android.util.Pair;

import com.bitshares.bitshareswallet.market.MarketTicker;
import com.bitshares.bitshareswallet.market.MarketTrade;
import com.bitshares.bitshareswallet.wallet.exception.NetworkStatusException;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.block_header;
import com.bitshares.bitshareswallet.wallet.graphene.chain.bucket_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.dynamic_global_property_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.global_config_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.global_property_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.limit_order_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.object_id;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operation_history_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operations;
import com.bitshares.bitshareswallet.wallet.graphene.chain.signed_transaction;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.models.constants.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.bitshares.bitshareswallet.wallet.common.ErrorCode.*;

public class websocket_api extends WebSocketListener {
    public interface BitsharesNoticeListener {
        void onNoticeMessage(BitsharesNoticeMessage message);
        void onDisconnect();
    }


    private int _nDatabaseId = -1;
    private int _nHistoryId = -1;
    private int _nBroadcastId = -1;

    private OkHttpClient mOkHttpClient;
    private WebSocket mWebsocket;

    private int mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
    private static int WEBSOCKET_CONNECT_INVALID = -1;
    private static int WEBSOCKET_CONNECT_SUCCESS = 0;
    private static int WEBSOCKET_ALL_READY = 0;
    private static int WEBSOCKET_CONNECT_FAIL = 1;

    private static int METHOD_CALL = 0;
    private static int METHOD_NOTICE = 1;

    private AtomicInteger mnCallId = new AtomicInteger(1);
    private Map<Integer, IReplyObjectProcess> mHashMapIdToProcess = new ConcurrentHashMap<>();

    private BitsharesNoticeListener mListener;

    private Set<Integer> msetMarketSubscription;
    private Set<Integer> msetSubscriptionCallback;

    private ExecutorService mExecutorService;
    private Set<Pair<object_id<asset_object>, object_id<asset_object>>> hashSetSubsribeMarket;

    class BitsharesNoticeMessageDeserializer implements JsonDeserializer<BitsharesNoticeMessage>  {

        @Override
        public BitsharesNoticeMessage deserialize(JsonElement json,
                                                  Type typeOfT,
                                                  JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray jsonArrayParams = jsonObject.get("params").getAsJsonArray();

            BitsharesNoticeMessage bitsharesNoticeMessage = new BitsharesNoticeMessage();
            int nSubscripitonid = jsonArrayParams.get(0).getAsInt();
            if (msetMarketSubscription.contains(nSubscripitonid)) {
                bitsharesNoticeMessage.nSubscriptionId = nSubscripitonid;
                JsonArray jsonArray = jsonArrayParams.get(1).getAsJsonArray().get(0).getAsJsonArray();
                if (jsonArray.get(0).isJsonArray() && jsonArray.get(0).getAsJsonArray().get(0).isJsonArray()) {
                    JsonElement jsonElement = jsonArray.get(0);
                    bitsharesNoticeMessage.listFillOrder = context.deserialize(
                            jsonElement,
                            new TypeToken<List<operations.operation_type>>(){}.getType()
                    );
                } else if (jsonArray.get(0).isJsonObject()){
                    JsonElement jsonElement = jsonArrayParams.get(1).getAsJsonArray().get(0);
                    bitsharesNoticeMessage.listOrderObject = context.deserialize(
                            jsonElement,
                            new TypeToken<List<limit_order_object>>(){}.getType()
                    );
                } else {

                }
            } else if (msetSubscriptionCallback.contains(nSubscripitonid)) {
                bitsharesNoticeMessage.nSubscriptionId = nSubscripitonid;
                bitsharesNoticeMessage.bAccountChanged = true;
            }

            return bitsharesNoticeMessage;
        }
    }



    /*
     WS_NODE_LIST: [
        {url: "wss://fake.automatic-selection.com", location: {translate: "settings.api_closest"}},
        {url: "ws://127.0.0.1:8090", location: "Locally hosted"},
        {url: "wss://bitshares.openledger.info/ws", location: "Nuremberg, Germany"},
        {url: "wss://eu.openledger.info/ws", location: "Berlin, Germany"},
        {url: "wss://bit.btsabc.org/ws", location: "Hong Kong"},
        {url: "wss://bts.transwiser.com/ws", location: "Hangzhou, China"},
        {url: "wss://bitshares.dacplay.org/ws", location:  "Hangzhou, China"},
        {url: "wss://bitshares-api.wancloud.io/ws", location:  "China"},
        {url: "wss://openledger.hk/ws", location: "Hong Kong"},
        {url: "wss://secure.freedomledger.com/ws", location: "Toronto, Canada"},
        {url: "wss://dexnode.net/ws", location: "Dallas, USA"},
        {url: "wss://altcap.io/ws", location: "Paris, France"},
        {url: "wss://bitshares.crypto.fans/ws", location: "Munich, Germany"},
        {url: "wss://node.testnet.bitshares.eu", location: "Public Testnet Server (Frankfurt, Germany)"}
         */

    public websocket_api(BitsharesNoticeListener listener) {
        msetMarketSubscription = Sets.newConcurrentHashSet();
        msetSubscriptionCallback = Sets.newConcurrentHashSet();
        hashSetSubsribeMarket = Sets.newConcurrentHashSet();

        global_config_object.getInstance().getGsonBuilder().registerTypeAdapter(
                BitsharesNoticeMessage.class,
                new BitsharesNoticeMessageDeserializer()
        );
        mListener = listener;
    }

    class WebsocketError {
        int code;
        String message;
        Object data;
    }

    class Call {
        int id;
        String method;
        List<Object> params;
    }

    class Reply<T> {
        String id;
        String jsonrpc;
        T result;
        WebsocketError error;
    }

    class ReplyBase {
        int id;
        String jsonrpc;
    }


    private interface IReplyObjectProcess<T> {
        void processTextToObject(String strText);
        T getReplyObject();
        String getError();
        void notifyFailure(Throwable t);
        Throwable getException();
        String getResponse();
    }

    private class ReplyObjectProcess<T> implements IReplyObjectProcess<T> {
        private String strError;
        private T mT;
        private Type mType;
        private Throwable exception;
        private String strResponse;
        public ReplyObjectProcess(Type type) {
            mType = type;
        }

        public void processTextToObject(String strText) {
            try {
                Gson gson = global_config_object.getInstance().getGsonBuilder().create();
                mT = gson.fromJson(strText, mType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                strError = e.getMessage();
                strResponse = strText;
            } catch (Exception e) {
                e.printStackTrace();
                strError = e.getMessage();
                strResponse = strText;
            }
            synchronized (this) {
                notify();
            }
        }

        @Override
        public T getReplyObject() {
            return mT;
        }

        @Override
        public String getError() {
            return strError;
        }

        @Override
        public void notifyFailure(Throwable t) {
            exception = t;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public Throwable getException() {
            return exception;
        }

        @Override
        public String getResponse() {
            return strResponse;
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        synchronized (mWebsocket) {
            mnConnectStatus = WEBSOCKET_CONNECT_SUCCESS;
            mWebsocket.notify();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (t instanceof IOException) {  // 出现io错误
            synchronized (mWebsocket) {
                mnConnectStatus = WEBSOCKET_CONNECT_FAIL;
                mWebsocket.notify();
            }
            synchronized (mHashMapIdToProcess) {
                for (Map.Entry<Integer, IReplyObjectProcess> entry : mHashMapIdToProcess.entrySet()) {
                    entry.getValue().notifyFailure(t);
                }
                mHashMapIdToProcess.clear();
            }
            if (mListener != null) {
                mListener.onDisconnect();
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        //super.onMessage(webSocket, text);

        try {
            int nMethod = 0;
            JSONObject jsonObject = new JSONObject(text);
            if (jsonObject.has("method")) {
                nMethod = METHOD_NOTICE;
            }

            if (nMethod == METHOD_CALL) {
                Gson gson = new Gson();
                ReplyBase replyObjectBase = gson.fromJson(text, ReplyBase.class);

                IReplyObjectProcess iReplyObjectProcess = null;
                synchronized (mHashMapIdToProcess) {
                    if (mHashMapIdToProcess.containsKey(replyObjectBase.id)) {
                        iReplyObjectProcess = mHashMapIdToProcess.get(replyObjectBase.id);
                    }
                }

                if (iReplyObjectProcess != null) {
                    iReplyObjectProcess.processTextToObject(text);
                } else {

                }
            } else {
                // process notice
                Gson gson = global_config_object.getInstance().getGsonBuilder().create();
                final BitsharesNoticeMessage bitsharesNoticeMessage = gson.fromJson(text, BitsharesNoticeMessage.class);

                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        // 将数据回调
                        mListener.onNoticeMessage(bitsharesNoticeMessage);
                    }
                });
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized int connect() {
        if (mnConnectStatus == WEBSOCKET_ALL_READY) {
            return 0;
        }

        FullNodeServerSelect fullNodeServerSelect = new FullNodeServerSelect();
        String strServer = fullNodeServerSelect.getServer();
        if (TextUtils.isEmpty(strServer)) {
            return ERROR_CONNECT_SERVER_FAILD;
        }

        Request request = new Request.Builder().url(strServer).build();
        mOkHttpClient = new OkHttpClient();
        mWebsocket = mOkHttpClient.newWebSocket(request, this);
        synchronized (mWebsocket) {
            if (mnConnectStatus == WEBSOCKET_CONNECT_INVALID) {
                try {
                    mWebsocket.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
                    return ERROR_CONNECT_SERVER_FAILD;
                }
            }
        }

        int nRet = 0;
        try {
            msetSubscriptionCallback.clear();
            boolean bLogin = login(Common.BTC_NODE_LOGIN, Common.BTC_NODE_PASS);
            if (bLogin == true) {
                _nDatabaseId = get_websocket_bitshares_api_id("database");
                _nHistoryId = get_websocket_bitshares_api_id("history");
                _nBroadcastId = get_websocket_bitshares_api_id("network_broadcast");
                //int _nNetworkId = get_websocket_bitshares_api_id("network_node");
                set_subscribe_callback();
                for (Pair<object_id<asset_object>, object_id<asset_object>> subscribe : hashSetSubsribeMarket) {
                    subscribe_to_market_impl(subscribe.first, subscribe.second);
                }

            } else {
                nRet = ERROR_CONNECT_SERVER_FAILD;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            nRet = ERROR_CONNECT_SERVER_FAILD;
        }

//        try {
//            asset_object base = lookup_asset_symbols("BTS");
//            asset_object quote = lookup_asset_symbols("CNY");
//            subscribe_to_market(base.id, quote.id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (nRet != 0) {
            mWebsocket.close(1000, "");
            mWebsocket = null;
            mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
        } else {
            mnConnectStatus = WEBSOCKET_ALL_READY;
        }

        mExecutorService = Executors.newSingleThreadExecutor();

        return nRet;
    }

    public synchronized int close() {
        synchronized (mHashMapIdToProcess) {
            for (Map.Entry<Integer, IReplyObjectProcess> entry : mHashMapIdToProcess.entrySet()) {
                synchronized (entry.getValue()) {
                    entry.getValue().notify();
                }
            }
        }


        mWebsocket.close(1000, "Close");
        mOkHttpClient = null;
        mWebsocket = null;
        mnConnectStatus = WEBSOCKET_CONNECT_INVALID;

        _nDatabaseId = -1;
        _nBroadcastId = -1;
        _nHistoryId = -1;

        mExecutorService.shutdown();

        return 0;
    }

    private boolean login(String strUserName, String strPassword) throws NetworkStatusException {
        Call callObject = new Call();

        callObject.id = mnCallId.getAndIncrement();;
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add("login");

        List<Object> listLoginParams = new ArrayList<>();
        listLoginParams.add(strUserName);
        listLoginParams.add(strPassword);
        callObject.params.add(listLoginParams);

        ReplyObjectProcess<Reply<Boolean>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<Boolean>>(){}.getType());
        Reply<Boolean> replyLogin = sendForReplyImpl(callObject, replyObject);


        return replyLogin.result;
    }

    private int get_websocket_bitshares_api_id(String strApiName) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add(strApiName);

        List<Object> listDatabaseParams = new ArrayList<>();
        callObject.params.add(listDatabaseParams);

        ReplyObjectProcess<Reply<Integer>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>(){}.getType());
        Reply<Integer> replyApiId = sendForReplyImpl(callObject, replyObject);

        return replyApiId.result;
    }

    private int get_database_api_id() throws NetworkStatusException {
        _nDatabaseId = get_websocket_bitshares_api_id("database");
        return _nDatabaseId;
    }

    private int get_history_api_id() throws NetworkStatusException {
        _nHistoryId = get_websocket_bitshares_api_id("history");
        return _nHistoryId;
    }

    private int get_broadcast_api_id() throws NetworkStatusException {
        _nBroadcastId = get_websocket_bitshares_api_id("network_broadcast");
        return _nBroadcastId;
    }

    public sha256_object get_chain_id() throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_chain_id");

        List<Object> listDatabaseParams = new ArrayList<>();

        callObject.params.add(listDatabaseParams);

        ReplyObjectProcess<Reply<sha256_object>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<sha256_object>>(){}.getType());
        Reply<sha256_object> replyDatabase = sendForReply(callObject, replyObject);

        return replyDatabase.result;
    }

    public List<account_object> lookup_account_names(String strAccountName) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_account_names");

        List<Object> listAccountNames = new ArrayList<>();
        listAccountNames.add(strAccountName);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountNames);

        callObject.params.add(listAccountNamesParams);

        ReplyObjectProcess<Reply<List<account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<account_object>>>(){}.getType());
        Reply<List<account_object>> replyAccountObjectList = sendForReply(callObject, replyObject);

        return replyAccountObjectList.result;
    }

    public List<account_object> get_accounts(List<object_id<account_object>> listAccountObjectId) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_accounts");

        List<Object> listAccountIds = new ArrayList<>();
        listAccountIds.add(listAccountObjectId);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountIds);

        callObject.params.add(listAccountIds);
        ReplyObjectProcess<Reply<List<account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<account_object>>>(){}.getType());
        Reply<List<account_object>> replyAccountObjectList = sendForReply(callObject, replyObject);

        return replyAccountObjectList.result;
    }

    public List<asset> list_account_balances(object_id<account_object> accountId) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_account_balances");

        List<Object> listAccountBalancesParam = new ArrayList<>();
        listAccountBalancesParam.add(accountId);
        listAccountBalancesParam.add(new ArrayList<Object>());
        callObject.params.add(listAccountBalancesParam);


        ReplyObjectProcess<Reply<List<asset>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset>>>(){}.getType());
        Reply<List<asset>> replyLookupAccountNames = sendForReply(callObject, replyObject);

        return replyLookupAccountNames.result;
    }

    public List<operation_history_object> get_account_history(object_id<account_object> accountId,
                                                              object_id<operation_history_object> startId,
                                                              int nLimit) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_account_history");

        List<Object> listAccountHistoryParam = new ArrayList<>();
        listAccountHistoryParam.add(accountId);
        listAccountHistoryParam.add(startId);
        listAccountHistoryParam.add(nLimit);
        listAccountHistoryParam.add("1.11.0");
        callObject.params.add(listAccountHistoryParam);

        ReplyObjectProcess<Reply<List<operation_history_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<operation_history_object>>>(){}.getType());
        Reply<List<operation_history_object>> replyAccountHistory = sendForReply(callObject, replyObject);

        return replyAccountHistory.result;
    }

    public global_property_object get_global_properties() throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_global_properties");

        callObject.params.add(new ArrayList<>());

        ReplyObjectProcess<Reply<global_property_object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<global_property_object>>(){}.getType());
        Reply<global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public dynamic_global_property_object get_dynamic_global_properties() throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_dynamic_global_properties");

        callObject.params.add(new ArrayList<Object>());

        ReplyObjectProcess<Reply<dynamic_global_property_object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<dynamic_global_property_object>>(){}.getType());
        Reply<dynamic_global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;

    }

    public List<asset_object> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("list_assets");

        List<Object> listAssetsParam = new ArrayList<>();
        listAssetsParam.add(strLowerBound);
        listAssetsParam.add(nLimit);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public List<asset_object> get_assets(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_assets");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listObjectId = new ArrayList<>();
        listObjectId.addAll(listAssetObjectId);

        listAssetsParam.add(listObjectId);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public asset_object lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_asset_symbols");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listAssetSysmbols = new ArrayList<>();
        listAssetSysmbols.add(strAssetSymbol);

        listAssetsParam.add(listAssetSysmbols);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result.get(0);
    }

    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_block_header");
        List<Object> listBlockNumber = new ArrayList<>();
        listBlockNumber.add(nBlockNumber);
        callObject.params.add(listBlockNumber);

        ReplyObjectProcess<Reply<block_header>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<block_header>>(){}.getType());
        Reply<block_header> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;

    }

    public int getLatestBlockHeight() throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(5);
        callObject.params.add("get_info");

        ReplyObjectProcess<Reply<block_header>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<block_header>>(){}.getType());
        Reply<block_header> replyObject = sendForReply(callObject, replyObjectProcess);

        return 666;
    }


    public int broadcast_transaction(signed_transaction tx) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nBroadcastId);
        callObject.params.add("broadcast_transaction");
        List<Object> listTransaction = new ArrayList<>();
        listTransaction.add(tx);
        callObject.params.add(listTransaction);

        ReplyObjectProcess<Reply<Object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>(){}.getType());
        Reply<Object> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject.error != null) {
            throw new NetworkStatusException(replyObject.error.message);
        } else {
            return 0;
        }
    }

    public List<bucket_object> get_market_history(object_id<asset_object> assetObjectId1,
                                                  object_id<asset_object> assetObjectId2,
                                                  int nBucket,
                                                  Date dateStart,
                                                  Date dateEnd) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_market_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(assetObjectId1);
        listParams.add(assetObjectId2);
        listParams.add(nBucket);
        listParams.add(dateStart);
        listParams.add(dateEnd);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<bucket_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<bucket_object>>>(){}.getType());
        Reply<List<bucket_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;

    }

    public List<limit_order_object> get_limit_orders(object_id<asset_object> base,
                                                     object_id<asset_object> quote,
                                                     int limit) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_limit_orders");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        Reply<List<limit_order_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public void subscribe_to_market(object_id<asset_object> base, object_id<asset_object> quote)
            throws NetworkStatusException {
        if (hashSetSubsribeMarket.contains(new Pair<>(base, quote))) {
            return;
        }

        subscribe_to_market_impl(base, quote);
        return;
    }

    public synchronized void subscribe_to_market_impl(object_id<asset_object> base, object_id<asset_object> quote)
            throws NetworkStatusException {

        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("subscribe_to_market");

        List<Object> listParams = new ArrayList<>();
        listParams.add(callObject.id);
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<Object>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<String>>(){}.getType());
        Reply<Object> reply = sendForReplyImpl(callObject, replyObject);

        msetMarketSubscription.add(callObject.id);

        hashSetSubsribeMarket.add(new Pair<>(base, quote));

        return;
    }

    public void set_subscribe_callback() throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("set_subscribe_callback");

        List<Object> listParams = new ArrayList<>();
        listParams.add(callObject.id);
        listParams.add(false);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<Object>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<String>>(){}.getType());
        Reply<Object> reply = sendForReplyImpl(callObject, replyObject);

        msetSubscriptionCallback.add(callObject.id);
    }

    public MarketTicker get_ticker(String base, String quote) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_ticker");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<MarketTicker>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<MarketTicker>>(){}.getType());
        Reply<MarketTicker> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_trade_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(start);
        listParams.add(end);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<MarketTrade>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<MarketTrade>>>(){}.getType());
        Reply<List<MarketTrade>> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public List<full_account_object> get_full_accounts(List<String> names, boolean subscribe)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_full_accounts");

        List<Object> listParams = new ArrayList<>();
        listParams.add(names);
        listParams.add(subscribe);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<full_account_object_reply>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<full_account_object_reply>>>(){}.getType());
        Reply<List<full_account_object_reply>> reply = sendForReply(callObject, replyObject);

        List<full_account_object> fullAccountObjectList = new ArrayList<>();
        for (full_account_object_reply fullAccountObjectReply : reply.result) {
            fullAccountObjectList.add(fullAccountObjectReply.fullAccountObject);
        }

        return fullAccountObjectList;
    }

    public List<limit_order_object> get_limit_orders(List<object_id<limit_order_object>> ids)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(ids);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        Reply<List<limit_order_object>> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public limit_order_object get_limit_order(object_id<limit_order_object> id)
            throws NetworkStatusException {
        return get_limit_orders(Collections.singletonList(id)).get(0);
    }

    public List<Integer> get_market_history_buckets() throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_market_history_buckets");

        List<Object> listParams = new ArrayList<>();
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<Integer>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<Integer>>>(){}.getType());
        Reply<List<Integer>> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    private <T> Reply<T> sendForReply(Call callObject,
                               ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        if (mWebsocket == null || mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
            int nRet = connect();
            if (nRet == -1) {
                throw new NetworkStatusException("It doesn't connect to the server.");
            }
        }

        return sendForReplyImpl(callObject, replyObjectProcess);
    }

    private <T> Reply<T> sendForReplyImpl(Call callObject,
                                      ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(callObject);

        synchronized (mHashMapIdToProcess) {
            mHashMapIdToProcess.put(callObject.id, replyObjectProcess);
        }

        synchronized (replyObjectProcess) {
            boolean bRet = mWebsocket != null && mWebsocket.send(strMessage);
            if (!bRet) {
                throw new NetworkStatusException("Failed to send message to server.");
            }

            try {
                replyObjectProcess.wait();
                Reply<T> replyObject = replyObjectProcess.getReplyObject();
                String strError = replyObjectProcess.getError();
                if (TextUtils.isEmpty(strError) == false) {
                    throw new NetworkStatusException(strError);
                } else if (replyObjectProcess.getException() != null) {
                    throw new NetworkStatusException(replyObjectProcess.getException());
                } else if (replyObject == null) {
                    throw new NetworkStatusException("Reply object is null.\n" + replyObjectProcess.getResponse());
                }else if (replyObject.error != null) {
                    throw new NetworkStatusException(gson.toJson(replyObject.error));
                }

                return replyObject;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
