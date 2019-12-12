package com.guarda.ethereum.dependencies;


import com.google.gson.Gson;
import com.guarda.ethereum.rest.GuardaLoggingApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.guarda.ethereum.models.constants.Guarda.GUARDA_INRETNAL;
import static com.guarda.ethereum.models.constants.Guarda.GUARDA_LOGGING;

@Module
public class RetrofitServicesModule {

    @Singleton
    @Provides
    GuardaLoggingApi provideGuardaLogging(OkHttpClient client, GsonConverterFactory factory) {
        return provideService(client, factory, GUARDA_LOGGING + GUARDA_INRETNAL, GuardaLoggingApi.class);
    }


    @Provides
    OkHttpClient provideOkHttpClientDefault() {
        return new okhttp3.OkHttpClient.Builder().build();
    }

    private Retrofit createRetrofit(
            OkHttpClient okhttpClient,
            GsonConverterFactory converterFactory,
            String baseUrl
    ) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okhttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private <T> T provideService(
            OkHttpClient client,
            GsonConverterFactory converterFactory,
            String baseUrl,
            Class<T> classOfT
    ) {
        return createRetrofit(client, converterFactory, baseUrl).create(classOfT);
    }

    @Singleton
    @Provides
    Gson provideGson() {
        return new Gson();
    }

    @Singleton
    @Provides
    GsonConverterFactory provideGsonConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

}
