package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.guarda.LogMessageRequest;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GuardaLoggingApi {

    @POST("logger")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Observable<ResponseBody> sendMessage(@Body LogMessageRequest logMessageRequest);

}
