package com.guarda.ethereum.managers;

import android.text.TextUtils;
import android.util.Log;

import com.bitshares.bitshareswallet.wallet.BitsharesNoticeMessage;
import com.bitshares.bitshareswallet.wallet.account_object;
import com.bitshares.bitshareswallet.wallet.asset;
import com.bitshares.bitshareswallet.wallet.authority;
import com.bitshares.bitshareswallet.wallet.common.ErrorCode;
import com.bitshares.bitshareswallet.wallet.exception.NetworkStatusException;
import com.bitshares.bitshareswallet.wallet.faucet.CreateAccountException;
import com.bitshares.bitshareswallet.wallet.faucet.create_account_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.dynamic_global_property_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.fee_schedule;
import com.bitshares.bitshareswallet.wallet.graphene.chain.global_config_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.global_property_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.memo_data;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operation_history_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operations;
import com.bitshares.bitshareswallet.wallet.graphene.chain.price;
import com.bitshares.bitshareswallet.wallet.graphene.chain.signed_transaction;
import com.bitshares.bitshareswallet.wallet.graphene.chain.types;
import com.bitshares.bitshareswallet.wallet.private_key;
import com.bitshares.bitshareswallet.wallet.websocket_api;
import com.bitshares.bitshareswallet.wallet.graphene.chain.object_id;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.guarda.ethereum.models.constants.Common;
import com.bitshares.bitshareswallet.wallet.graphene.chain.block_header;


import org.json.JSONObject;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.bitshares.bitshareswallet.wallet.common.ErrorCode.ERROR_NETWORK_FAIL;
import static com.bitshares.bitshareswallet.wallet.common.ErrorCode.ERROR_SERVER_CREATE_ACCOUNT_FAIL;
import static com.bitshares.bitshareswallet.wallet.common.ErrorCode.ERROR_SERVER_RESPONSE_FAIL;
import static com.bitshares.bitshareswallet.wallet.common.ErrorCode.ERROR_UNKNOWN;

public class BtsManager {

    public static BtsManager getInstance() {
        if (instance_s == null) {
            instance_s = new BtsManager();
            instance_s.init();
        }
        return instance_s;
    }



    private void init() {
        mWebsocketApi = new websocket_api(new websocket_api.BitsharesNoticeListener() {
            @Override
            public void onNoticeMessage(BitsharesNoticeMessage message) {
            }

            @Override
            public void onDisconnect() {
            }
        });
    }



    public String importAccountPassword(String strAccountName, String strPassword) {
        Log.d("flint", "BtsManager.strAccountName: " + strAccountName);
        private_key privateActiveKey = private_key.from_seed(strAccountName + "active" + strPassword);
        private_key privateOwnerKey = private_key.from_seed(strAccountName + "owner" + strPassword);
        types.public_key_type publicActiveKeyType = new types.public_key_type(privateActiveKey.get_public_key());
        types.public_key_type publicOwnerKeyType = new types.public_key_type(privateOwnerKey.get_public_key());

        account_object acc = isNameRegistered(strAccountName);
        if (acc != null)
            if (acc.active.is_public_key_type_exist(publicActiveKeyType) ||
                acc.owner.is_public_key_type_exist(publicActiveKeyType) ||
                acc.active.is_public_key_type_exist(publicOwnerKeyType) ||
                acc.owner.is_public_key_type_exist(publicOwnerKeyType) )
                return acc.id.toString();
        return null;
    }



    public long getBlockTime(int blockNum) {
        try {
            block_header blockHeader = mWebsocketApi.get_block_header(blockNum);
            return blockHeader.timestamp.getTime()/1000;
        } catch (Exception e) {
            return new Date().getTime();
        }
    }



    public int getLatestBlockHeight() {
        try {
            return mWebsocketApi.getLatestBlockHeight();
        } catch (Exception e) {
            return 100;
        }
    }



    public void getLatestBlockHeightFromExplorer(final Callback<Integer> callback) {
        makeGetQueryEx("http://23.94.69.140:5000", "/header", new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    JSONObject jsonResp = new JSONObject(resp);
                    int head_block_number = jsonResp.getInt("head_block_number");
                    if (callback != null)
                        callback.onResponse(head_block_number);
                } catch (Exception e) {
                    Log.e("flint", "BtsManager.getLatestBlockHeightFromExplorer()... exception: " + e.toString());
                }
            }
        });
    }



    public private_key getPrivateKeyForSigning(String strAccountName, String strPassword) {
        return private_key.from_seed(strAccountName + "active" + strPassword);
    }



    public account_object isNameRegistered(String strAccountName) {
        try {
            List<account_object> accountObjects = lookup_account_names(strAccountName);
            if (accountObjects.size() > 0)
                if (accountObjects.get(0) != null)
                    return accountObjects.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public account_object get_account(String strAccountName) throws NetworkStatusException {
        return isNameRegistered(strAccountName);
    }



    public long getBalance(String strAccountName) {
        account_object acc = isNameRegistered(strAccountName);
        if (acc != null) {
            List<asset> res = null;
            try {
                res = mWebsocketApi.list_account_balances(acc.id);
            } catch (NetworkStatusException e) {
                res = null;
            }
            if (res != null) {
                if (res.size() > 0) {
                    asset a = res.get(0);
                    return a.amount;
                } else {
                    return 0;
                }
            }
        }
        return -1;
    }



    public List<operation_history_object> getTransactions(String strAccountName) {
        account_object acc = isNameRegistered(strAccountName);
        List<operation_history_object> res = null;
        if (acc != null) {
            try {
                object_id<operation_history_object> startId = new object_id<>(0, operation_history_object.class);
                res = mWebsocketApi.get_account_history(acc.id, startId, 100);
            } catch (NetworkStatusException e) {
                res = null;
            }
        }
        return res;
    }



    private List<account_object> lookup_account_names(String strAccountName) {
        try {
            mWebsocketApi.connect();
            List<account_object> accountObjects = mWebsocketApi.lookup_account_names(strAccountName);
            return accountObjects;
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return null;
        }
    }



    public String get_account_name_by_id(String strAccountId) {
        try {
            mWebsocketApi.connect();
            object_id<account_object> accountObjectobjectId = object_id.create_from_string(strAccountId);
            List<account_object> accountObjects = mWebsocketApi.get_accounts(Arrays.asList(accountObjectobjectId));
            return accountObjects.get(0).name;
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return strAccountId;
        }
    }



    public void createNewWallet(final String strAccountName, final String strPassword, final Callback<Integer> callback) throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                private_key privateActiveKey = private_key.from_seed(strAccountName + "active" + strPassword);
                private_key privateOwnerKey = private_key.from_seed(strAccountName + "owner" + strPassword);

                types.public_key_type publicActiveKeyType = new types.public_key_type(privateActiveKey.get_public_key());
                types.public_key_type publicOwnerKeyType = new types.public_key_type(privateOwnerKey.get_public_key());

                account_object accountObject = isNameRegistered(strAccountName);
                if (accountObject != null) {
                    callback.onResponse(ErrorCode.ERROR_ACCOUNT_OBJECT_EXIST);
                    return;
                }

                create_account_object createAccountObject = new create_account_object();
                createAccountObject.name = strAccountName;
                createAccountObject.active_key = publicActiveKeyType;
                createAccountObject.owner_key = publicOwnerKeyType;
                createAccountObject.memo_key = publicActiveKeyType;
                createAccountObject.refcode = null;
                createAccountObject.referrer = "bituniverse";
                Gson gson = global_config_object.getInstance().getGsonBuilder().create();

                String strAddress = "https://openledger.io/api/v1/accounts";
                OkHttpClient okHttpClient = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(createAccountObject)
                );

                Request request = new Request.Builder()
                        .url(strAddress)
                        .addHeader("Accept", "application/json")
                        .post(requestBody)
                        .build();

                create_account_object.create_account_response createAccountResponse = null;
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        createAccountResponse = gson.fromJson(
                                response.body().string(),
                                create_account_object.create_account_response.class
                        );
                    } else {
                        if (response.body().contentLength() != 0) {
                            String strResponse = response.body().string();

                            try {
                                create_account_object.response_fail_error error = gson.fromJson(
                                        strResponse,
                                        create_account_object.response_fail_error.class
                                );
                                for (Map.Entry<String, List<String>> errorEntrySet : error.error.entrySet()) {
                                    callback.onResponse(ERROR_UNKNOWN);
                                    return;
                                }
                            } catch (JsonSyntaxException e) {
                                callback.onResponse(ERROR_UNKNOWN);
                                return;
                            }
                        }

                        callback.onResponse(ERROR_SERVER_RESPONSE_FAIL);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    callback.onResponse(ERROR_NETWORK_FAIL);
                    return;
                }

                if (createAccountResponse.account != null) {
                    callback.onResponse(0);
                    return;
                } else {
                    if (createAccountResponse.error.base.isEmpty() == false) {
                        String strError = createAccountResponse.error.base.get(0);
                        callback.onResponse(ERROR_UNKNOWN);
                        return;
                    }
                    callback.onResponse(ERROR_SERVER_CREATE_ACCOUNT_FAIL);
                    return;
                }
            }
        });
        thread.start();
    }



    public String generateRandomPassword() {
        byte[] randomSeed = new byte[32];
        new SecureRandom().nextBytes(randomSeed);
        private_key randomPrivKey = new private_key(randomSeed);
        types.public_key_type randomPublicKey = new types.public_key_type(randomPrivKey.get_public_key());
        String pswd = randomPublicKey.toString();
        return pswd.substring(6, 26);
    }



    public signed_transaction transfer(String strFrom,
                                       String strTo,
                                       String strAmount,
                                       String strAssetSymbol,
                                       String strMemo,
                                       String senderAccountName,
                                       String senderPassword) throws NetworkStatusException {

        object_id<asset_object> assetObjectId = object_id.create_from_string(strAssetSymbol);
        asset_object assetObject = null;
        if (assetObjectId == null) {
            assetObject = lookup_asset_symbols(strAssetSymbol);
        } else {
            List<object_id<asset_object>> listAssetObjectId = new ArrayList<>();
            listAssetObjectId.add(assetObjectId);
            assetObject = get_assets(listAssetObjectId).get(0);
        }

        account_object accountObjectFrom = get_account(strFrom);
        account_object accountObjectTo = get_account(strTo);
        if (accountObjectTo == null) {
            throw new NetworkStatusException("failed to get account object");
        }

        operations.transfer_operation transferOperation = new operations.transfer_operation();
        transferOperation.from = accountObjectFrom.id;
        transferOperation.to = accountObjectTo.id;
        transferOperation.amount = assetObject.amount_from_string(strAmount);
        transferOperation.extensions = new HashSet<>();
        if (TextUtils.isEmpty(strMemo) == false) {
            transferOperation.memo = new memo_data();
            transferOperation.memo.from = accountObjectFrom.options.memo_key;
            transferOperation.memo.to = accountObjectTo.options.memo_key;

            types.private_key_type privateKeyType = new types.private_key_type(getPrivateKeyForSigning(senderAccountName, senderPassword));
            if (privateKeyType == null) {
                // // TODO: 07/09/2017 获取失败的问题
                throw new NetworkStatusException("failed to get private key");
            }
            transferOperation.memo.set_message(
                    privateKeyType.getPrivateKey(),
                    accountObjectTo.options.memo_key.getPublicKey(),
                    strMemo,
                    0
            );
            transferOperation.memo.get_message(
                    privateKeyType.getPrivateKey(),
                    accountObjectTo.options.memo_key.getPublicKey()
            );
        }

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_TRANSER_OPERATION;
        operationType.operationContent = transferOperation;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);
        tx.extensions = new HashSet<>();
        set_operation_fees(tx, get_global_properties().parameters.current_fees);


        //// TODO: 07/09/2017 tx.validate();
        return sign_transaction(tx, senderAccountName, senderPassword);
    }



    private signed_transaction sign_transaction(signed_transaction tx, String senderAccountName, String senderPassword) throws NetworkStatusException {
        // // TODO: 07/09/2017 这里的set应出问题
        signed_transaction.required_authorities requiresAuthorities = tx.get_required_authorities();

        Set<object_id<account_object>> req_active_approvals = new HashSet<>();
        req_active_approvals.addAll(requiresAuthorities.active);

        Set<object_id<account_object>> req_owner_approvals = new HashSet<>();
        req_owner_approvals.addAll(requiresAuthorities.owner);


        for (authority authorityObject : requiresAuthorities.other) {
            for (object_id<account_object> accountObjectId : authorityObject.account_auths.keySet()) {
                req_active_approvals.add(accountObjectId);
            }
        }

        Set<object_id<account_object>> accountObjectAll = new HashSet<>();
        accountObjectAll.addAll(req_active_approvals);
        accountObjectAll.addAll(req_owner_approvals);


        List<object_id<account_object>> listAccountObjectId = new ArrayList<>();
        listAccountObjectId.addAll(accountObjectAll);

        List<account_object> listAccountObject = get_accounts(listAccountObjectId);
        HashMap<object_id<account_object>, account_object> hashMapIdToObject = new HashMap<>();
        for (account_object accountObject : listAccountObject) {
            hashMapIdToObject.put(accountObject.id, accountObject);
        }

        HashSet<types.public_key_type> approving_key_set = new HashSet<>();
        for (object_id<account_object> accountObjectId : req_active_approvals) {
            account_object accountObject = hashMapIdToObject.get(accountObjectId);
            approving_key_set.addAll(accountObject.active.get_keys());
        }

        for (object_id<account_object> accountObjectId : req_owner_approvals) {
            account_object accountObject = hashMapIdToObject.get(accountObjectId);
            approving_key_set.addAll(accountObject.owner.get_keys());
        }

        for (authority authorityObject : requiresAuthorities.other) {
            for (types.public_key_type publicKeyType : authorityObject.get_keys()) {
                approving_key_set.add(publicKeyType);
            }
        }

        // // TODO: 07/09/2017 被简化了
        dynamic_global_property_object dynamicGlobalPropertyObject = get_dynamic_global_properties();
        tx.set_reference_block(dynamicGlobalPropertyObject.head_block_id);

        Date dateObject = dynamicGlobalPropertyObject.time;
        Calendar calender = Calendar.getInstance();
        calender.setTime(dateObject);
        calender.add(Calendar.SECOND, 30);

        dateObject = calender.getTime();

        tx.set_expiration(dateObject);

//        for (types.public_key_type pulicKeyType : approving_key_set) {
//            types.private_key_type privateKey = mHashMapPub2Priv.get(pulicKeyType);
//            if (privateKey != null) {
//                tx.sign(privateKey, mWalletObject.chain_id);
//            }
//        }
        tx.sign(new types.private_key_type(getPrivateKeyForSigning(senderAccountName, senderPassword)), mWebsocketApi.get_chain_id());

        // 发出tx，进行广播，这里也涉及到序列化
        int nRet = mWebsocketApi.broadcast_transaction(tx);
        if (nRet == 0) {
            return tx;
        } else {
            return null;
        }
    }



    public dynamic_global_property_object get_dynamic_global_properties() throws NetworkStatusException {
        return mWebsocketApi.get_dynamic_global_properties();
    }



    public List<account_object> get_accounts(List<object_id<account_object>> listAccountObjectId) throws NetworkStatusException {
        return mWebsocketApi.get_accounts(listAccountObjectId);
    }



    public global_property_object get_global_properties() throws NetworkStatusException {
        return mWebsocketApi.get_global_properties();
    }



    private void set_operation_fees(signed_transaction tx, fee_schedule feeSchedule) {
        for (operations.operation_type operationType : tx.operations) {
            feeSchedule.set_fee(operationType, price.unit_price(new object_id<asset_object>(0, asset_object.class)));
        }
    }



    public List<asset_object> get_assets(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        return mWebsocketApi.get_assets(listAssetObjectId);
    }



    public asset_object lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        return mWebsocketApi.lookup_asset_symbols(strAssetSymbol);
    }




    private static void makeGetQueryEx(final String apiUrl, final String subUrl, final Callback2<String, String> callback) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient httpClient = new OkHttpClient.Builder()
                                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
                                .writeTimeout(TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
                                .readTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS)
                                .build();
                        String reqUrl = apiUrl + subUrl;
                        Request req = new Request.Builder().url(reqUrl).build();
                        Response resp = httpClient.newCall(req).execute();
                        String respString = resp.body().string();
                        callback.onResponse("ok", respString);
                    } catch (Exception e) {
                        callback.onResponse("error from OkHttpClient: " + e.toString(), "");
                    }
                }
            };
            final Thread taskThread = new Thread(runnable);
            taskThread.start();
        } catch (Exception e) {
            callback.onResponse("error: " + e.toString(), "");
        }
    }




    private static BtsManager instance_s = null;

    private websocket_api mWebsocketApi;

    private static final int TIMEOUT_CONNECT = 8000;
    private static final int TIMEOUT_WRITE = 8000;
    private static final int TIMEOUT_READ = 8000;

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

}
