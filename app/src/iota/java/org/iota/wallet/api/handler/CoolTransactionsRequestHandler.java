/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.iota.wallet.api.handler;

import android.content.Context;

import com.google.gson.Gson;
import com.guarda.ethereum.BuildConfig;

import org.iota.wallet.api.requests.ApiRequest;
import org.iota.wallet.api.requests.CoolTransationsRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.api.responses.CoolTransactionResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.api.responses.error.NetworkErrorType;
import org.iota.wallet.model.Transaction;

import java.io.IOException;

import jota.IotaAPI;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoolTransactionsRequestHandler extends IotaRequestHandler {
    public CoolTransactionsRequestHandler(IotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return CoolTransationsRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest apiRequest) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://iota.cool/load/current_transactions?max=500&tag=&spam=false&mst=false&nonzero=true&uncut=true")
                .build();
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                Transaction[] transactions = new Gson().fromJson(response.body().string(), Transaction[].class);
                return new CoolTransactionResponse(transactions);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        NetworkError error = new NetworkError();
        error.setErrorType(NetworkErrorType.IOTA_COOL_NETWORK_ERROR);
        return error;
    }
}
