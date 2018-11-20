package com.guarda.ethereum.rest;


import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.models.constants.BchExplorer;
import com.guarda.ethereum.models.constants.BlockChainInfo;
import com.guarda.ethereum.models.constants.BtgExplorer;
import com.guarda.ethereum.models.constants.Callistoexplorer;
import com.guarda.ethereum.models.constants.Changelly;
import com.guarda.ethereum.models.constants.Coinify;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Coinmarketcap;
import com.guarda.ethereum.models.constants.Cryptocompare;
import com.guarda.ethereum.models.constants.DgbExplorer;
import com.guarda.ethereum.models.constants.Etcchain;
import com.guarda.ethereum.models.constants.Etherscan;
import com.guarda.ethereum.models.constants.KmdExplorer;
import com.guarda.ethereum.models.constants.QtumExplorer;
import com.guarda.ethereum.models.constants.LtcExplorer;
import com.guarda.ethereum.models.constants.SbtcExplorer;
import com.guarda.ethereum.models.constants.Wemovecoins;
import com.guarda.ethereum.models.constants.ZecExplorer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.guarda.ethereum.models.constants.Common.NODE_ADDRESS;
import static com.guarda.ethereum.models.constants.Guarda.GUARDA_CO_URL;
import static com.guarda.ethereum.models.constants.Guarda.GUARDA_ETC_URL;
import static com.guarda.ethereum.models.constants.Guarda.GUARDA_ETH_URL;

public class ApiMethods {

    public interface RequestListener {
        void onSuccess(Object response);

        void onFailure(String msg);
    }

    static LightWalletApi createLightWalletApi(final HashMap<String, String> headerParam) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Content-type", "application/json");

                for (Map.Entry<String, String> entry : headerParam.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    requestBuilder.addHeader(key, value);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Changelly.CHANGELLY_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(LightWalletApi.class);
    }

    static BitcoinNodeApi createBitcoinNodeApi() {
        final String credentials = Credentials.basic(Common.BTC_NODE_LOGIN, Common.BTC_NODE_PASS);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Content-type", "application/json")
                        .addHeader("Authorization", credentials);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NODE_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(BitcoinNodeApi.class);
    }

    static EtcchainApi createEtcchainApi() {
        String url = new String(Etcchain.ETCCHAIN_BASE_URL);
        if (Build.VERSION.SDK_INT <= 18)
            url = url.replace("https://", "http://");
        return getBaseApi(url).create(EtcchainApi.class);
    }

    static GastrackerApi createGastrackerApi() {
        String url = new String("https://api.gastracker.io");
        if (Build.VERSION.SDK_INT <= 18)
            url = url.replace("https://", "http://");
        return getBaseApi(url).create(GastrackerApi.class);
    }


    static EtherScanApi createEtherScanApi() {
        String url = new String(Etherscan.ETHERSCAN_BASE_URL);
        if (Build.VERSION.SDK_INT <= 18)
            url = url.replace("https://", "http://");
        return getBaseApi(url).create(EtherScanApi.class);
    }

    static EthPlorerApi createEthplorerService() {
        String url = new String(Etherscan.ETHPLORER_BASE_URL);
        if (Build.VERSION.SDK_INT <= 18)
            url = url.replace("https://", "http://");
        return getBaseApi(url).create(EthPlorerApi.class);
    }

    static EthPlorerApi createEthplorerApi() {
        String url = new String(Etherscan.ETHPLORER_API_URL);
        if (Build.VERSION.SDK_INT <= 18)
            url = url.replace("https://", "http://");
        return getBaseApi(url).create(EthPlorerApi.class);
    }

    static CallistoExplorerApi createCallistoExplorerApiNew() {
//        final String credentials = Credentials.basic(Common.BTC_NODE_LOGIN, Common.BTC_NODE_PASS);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Content-type", "application/x-www-form-urlencoded");
//                        .addHeader("Authorization", credentials);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Callistoexplorer.CALLISTOEXPLORER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(CallistoExplorerApi.class);
    }

    static CallistoExplorerApi createCallistoExplorerApi() {
        return getBaseApi(Callistoexplorer.CALLISTOEXPLORER_BASE_URL).create(CallistoExplorerApi.class);
    }

    static NodeApi createNodeApi(){
        return getBaseApi(NODE_ADDRESS).create(NodeApi.class);
    }

    static CoinmarketcapApi createCoinmarketcapApi() {
        return getBaseApi(Coinmarketcap.COINMARKETCAP_BASE_URL).create(CoinmarketcapApi.class);
    }

    static CryptocompareApi createCryptocompareApi() {
        return getBaseApi(Cryptocompare.CRYPTOCOMPARE_BASE_URL).create(CryptocompareApi.class);
    }

    static WemovecoinsApi createWemovecoinApi() {
        return getBaseApi(Wemovecoins.WEMOVECOINS_BASE_URL).create(WemovecoinsApi.class);
    }

    static CoinifyApi createCoinifyApi() {
        if (BuildConfig.DEBUG) {
            return getBaseApi(Coinify.SANDBOX_COINIFY_BASE_URL).create(CoinifyApi.class);
        } else {
            return getBaseApi(Coinify.COINIFY_BASE_URL).create(CoinifyApi.class);
        }
    }

    static CoinifyApi createCoinifyApiAuth(String accessToken) {
        if (BuildConfig.DEBUG) {
            return getBaseApiAuth(Coinify.SANDBOX_COINIFY_BASE_URL, accessToken).create(CoinifyApi.class);
        } else {
            return getBaseApiAuth(Coinify.COINIFY_BASE_URL, accessToken).create(CoinifyApi.class);
        }
    }

    static BlockChainInfoApi createBlockChainInfoApi() {
        return getBaseApi(BlockChainInfo.BLOCKCHAIN_INFO_BASE_URL).create(BlockChainInfoApi.class);
    }

    static BtgApi createBtgApi() {
        return getBaseApi(BtgExplorer.BTG_EXPLORER_BASE_URL).create(BtgApi.class);
    }

    static BchApi createBchApi() {
        return getBaseApi(BchExplorer.BCHSV_EXPLORER_BASE_URL).create(BchApi.class);
    }

    static QtumApi createQtumApi() {
        return getBaseApi(QtumExplorer.QTUM_EXPLORER_BASE_URL).create(QtumApi.class);
    }

    static KmdApi createKmdApi() {
        return getBaseApi(KmdExplorer.KMD_EXPLORER_BASE_URL).create(KmdApi.class);
    }

    static SbtcApi createSbtcApi() {
        return getBaseApi(SbtcExplorer.SBTC_EXPLORER_BASE_URL).create(SbtcApi.class);
    }

    static LtcApi createLtcApi() {
        return getBaseApi(LtcExplorer.LTC_EXPLORER_BASE_URL).create(LtcApi.class);
    }

    static BtgApiNew createBtgApiNew() {
        return getBaseApi(BtgExplorer.BTG_EXPLORER_BASE_URL).create(BtgApiNew.class);
    }

    static DgbApiNew createDgbApiNew() {
        return getBaseApi(DgbExplorer.DGB_EXPLORER_BASE_URL).create(DgbApiNew.class);
    }

    static SbtcApiNew createSbtcApiNew() {
        return getBaseApi(SbtcExplorer.SBTC_EXPLORER_BASE_URL).create(SbtcApiNew.class);
    }

    static LtcApiNew createLtcApiNew() {
        return getBaseApi(LtcExplorer.LTC_EXPLORER_BASE_URL).create(LtcApiNew.class);
    }

    static ZecApiNew createZecApiNew() {
        return getBaseApi(ZecExplorer.ZEC_EXPLORER_BASE_URL).create(ZecApiNew.class);
    }

    static GuardaCoApi createGuardaCoApi() {
        return getBaseApi(GUARDA_CO_URL).create(GuardaCoApi.class);
    }

    static GuardaEthApi createGuardaEthApi() {
        return getBaseApi(GUARDA_ETH_URL).create(GuardaEthApi.class);
    }

    static GuardaEtcApi createGuardaEtcApi() {
        return getBaseApi(GUARDA_ETC_URL).create(GuardaEtcApi.class);
    }

    private static Retrofit getBaseApi(String baseUrl) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        OkHttpClient client = httpClient
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

    private static Retrofit getBaseApiAuth(String baseUrl, final String accessToken) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Authorization", "Bearer " + accessToken);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

    static void makeRequest(Call call, @Nullable final RequestListener listener) {
        call.enqueue(new Callback<Object>() {
                         @Override
                         public void onResponse(Call<Object> call, Response<Object> response) {
                             if (response.isSuccessful()) {
                                 //getting response from server
                                 Object serverResponse = response.body();
                                 if (listener != null) {
                                     listener.onSuccess(serverResponse);
                                 }
                             } else {
                                 String errBody = "";
                                 try {
                                     errBody = response.errorBody().string();
//                                     Log.d("svcom ", "clearText body " + response.errorBody().string());
                                     Log.d("svcom ", "clearText body " + errBody);
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                     listener.onFailure("Request clearText");
                                 }
                                 if (listener != null) {
                                     Log.d("psd ", "clearText body 1 " + errBody);
                                     listener.onFailure(errBody);
                                 }
                             }
                         }

                         @Override
                         public void onFailure(Call<Object> call, Throwable t) {
                             Log.d("svcom", "failure " + t.getMessage() + " " + t.toString());
                             if (listener != null) {
                                 listener.onFailure(t.getMessage() + " " + t.toString());
                             }
                         }
                     }
        );
    }

}
