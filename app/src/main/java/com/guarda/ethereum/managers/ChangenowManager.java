package com.guarda.ethereum.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guarda.ethereum.models.changenow.CurrencyResp;
import com.guarda.ethereum.models.changenow.EstimatedResp;
import com.guarda.ethereum.models.changenow.MinAmountResp;
import com.guarda.ethereum.models.changenow.TxResp;
import com.guarda.ethereum.models.constants.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class ChangenowManager {

    //ChangeNOW API
    private static ChNowManager chNowManager;
    private static Retrofit chNowRetrofit;

    public static ChNowManager changeNowApi() {
        if (chNowManager == null) {
            if (chNowRetrofit == null) {
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();
                chNowRetrofit = new Retrofit.Builder()
                        .baseUrl(Const.CHANGENOW_URL)
                        .client(getNewHttpClient())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
            }
            chNowManager = chNowRetrofit.create(ChNowManager.class);
        }

        return chNowManager;
    }

    public static ChangenowManager getInstance() {
        return instance_s;
    }

    public void updateSupportedCoinsList(final Callback<Boolean> callback) {
        ChangenowApi.getSupportedCoins(new Callback2<String, Map<String, ChangenowApi.SupportedCoinModel>>() {
            @Override
            public void onResponse(String status, Map<String, ChangenowApi.SupportedCoinModel> resp) {
                if ("ok".equals(status)) {
                    supportedCoins.clear();
                    for (String key : resp.keySet()) {
                        ChangenowApi.SupportedCoinModel coin = resp.get(key);
                        if (coin.isAbailable)
                            supportedCoins.add(resp.get(key));
                    }
                    if (callback != null)
                        callback.onResponse(true);
                } else {
                    if (callback != null)
                        callback.onResponse(false);
                }
            }
        });
    }

    public void getRate(final String fromCoin, final String toCoin, final Callback<ChangenowApi.GetRateRespModel> callback) {
        ChangenowApi.getRate(fromCoin, toCoin, new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    callback.onResponse(resp);
                else
                    callback.onResponse(new ChangenowApi.GetRateRespModel());
            }
        });
    }

    public void getMinAmount(final String fromCoin, final String toCoin, final Callback<ChangenowApi.GetRateRespModel> callback) {
        ChangenowApi.getMinAmount(fromCoin, toCoin, new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    callback.onResponse(resp);
                else
                    callback.onResponse(new ChangenowApi.GetRateRespModel());
            }
        });
    }

    //ChangeNOW
    public interface ChNowManager {
        @GET("currencies?active")
        Call<ArrayList<CurrencyResp>> currencies();

        @GET("market-info/available-pairs")
        Call<ArrayList<String>> pairs();

        @GET("market-info/available-pairs")
        Observable<ArrayList<String>> pairsObs();

        @GET("currencies-to/{ticker}")
        Call<ArrayList<CurrencyResp>> currenciesTo(@Path("ticker") String ticker);

        @GET("min-amount/{from}_{to}")
        Call<MinAmountResp> minAmount(@Path("from") String from, @Path("to") String to);

        @GET("exchange-amount/{amount}/{from}_{to}")
        Call<EstimatedResp> estimated(@Path("amount") float amount, @Path("from") String from, @Path("to") String to);

        @GET("exchange-amount/{amount}/{from}_{to}")
        Observable<EstimatedResp> estimatedObs(@Path("amount") float amount, @Path("from") String from, @Path("to") String to);

        @GET("transactions/{id}/{apikey}")
        Call<TxResp> txStatus(@Path("id") String id, @Path("apikey") String apikey);

        @POST("transactions/{apikey}")
        @FormUrlEncoded
        Call<TxResp> postTx(@Field("from") String from,
                            @Field("to") String to,
                            @Field("address") String address,
                            @Field("amount") String amount,
                            @Field("extraId") String extraId,
                            @Field("refundAddress") String refundAddress,
                            @Path("apikey") String apikey);

        @POST("transactions/{txId}/tokens-destination")
        @FormUrlEncoded
        Call<ResponseBody> postTokenDest(@Path("txId") String txId,
                                         @Field("tokensDestination") String tokenDest,
                                         @Field("currency") String currency);
    }

    private static OkHttpClient getNewHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);

        return client.build();
    }

    public List<ChangenowApi.SupportedCoinModel> getSupportedCoins() {
        return supportedCoins;
    }

    private static ChangenowManager instance_s = new ChangenowManager();

    private ArrayList<ChangenowApi.SupportedCoinModel> supportedCoins = new ArrayList<>();

}
