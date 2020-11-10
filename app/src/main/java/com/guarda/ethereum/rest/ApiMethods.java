package com.guarda.ethereum.rest;


import android.util.Log;

import androidx.annotation.Nullable;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.models.constants.Changelly;
import com.guarda.ethereum.models.constants.Coinmarketcap;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Cryptocompare;
import com.guarda.ethereum.models.constants.ZecExplorer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Common.NODE_ADDRESS;
import static com.guarda.ethereum.models.constants.Const.MAINNET_FLAVOR;

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

    static NodeApi createNodeApi(){
        return getBaseApi(NODE_ADDRESS).create(NodeApi.class);
    }

    static CoinmarketcapApi createCoinmarketcapApi() {
        return getBaseApi(Coinmarketcap.COINMARKETCAP_BASE_URL).create(CoinmarketcapApi.class);
    }

    static CryptocompareApi createCryptocompareApi() {
        return getBaseApi(Cryptocompare.CRYPTOCOMPARE_BASE_URL).create(CryptocompareApi.class);
    }

    static InsightApiNew createZecInsightApiNew() {
        if (BuildConfig.FLAVOR.equals(MAINNET_FLAVOR)) {
            return getBaseApi(ZecExplorer.ZEC_EXPLORER_API).create(InsightApiNew.class);
        } else {
            return getBaseApi(ZecExplorer.ZEC_EXPLORER_API_TESTNET).create(InsightApiNew.class);
        }
    }

    static ZecApiNew createZecApiNew() {
        if (BuildConfig.FLAVOR.equals(MAINNET_FLAVOR)) {
            return getBaseRxApi(ZecExplorer.ZEC_EXPLORER_API).create(ZecApiNew.class);
        } else {
            return getBaseRxApi(ZecExplorer.ZEC_EXPLORER_API_TESTNET).create(ZecApiNew.class);
        }
    }

    static ZecBookApi createZecBookApi() {
        if (BuildConfig.FLAVOR.equals(MAINNET_FLAVOR)) {
            return getBaseRxApi(ZecExplorer.ZEC_BOOK_API).create(ZecBookApi.class);
        } else {
            return getBaseRxApi(ZecExplorer.ZEC_BOOK_API_TESTNET).create(ZecBookApi.class);
        }
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

    private static Retrofit getBaseRxApi(String baseUrl) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = builder
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
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
        Timber.d("Request path=%s", call.request());
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
