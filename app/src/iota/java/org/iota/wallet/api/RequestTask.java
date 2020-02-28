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

package org.iota.wallet.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.preference.BuildConfig;
import androidx.appcompat.preference.PreferenceManager;
import android.telecom.Call;
import android.util.Log;

import com.google.gson.Gson;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.models.constants.Common;

import org.greenrobot.eventbus.EventBus;
import org.iota.wallet.api.requests.ApiRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.helper.Constants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

class RequestTask extends AsyncTask<ApiRequest, String, ApiResponse> {

    private WeakReference<Context> context;
    private EventBus               bus;
    private long             start = 0;
    private SimpleDateFormat sdf   = new SimpleDateFormat("HH:mm:ss.sss");
    private String           tag   = "";
    private Callback<Object> callback  = null;


    public RequestTask(Context context, Callback<Object> callback) {
        this.context = new WeakReference<>(context);
        this.bus = EventBus.getDefault();
        this.callback = callback;
    }

    @Override
    protected ApiResponse doInBackground(ApiRequest... params) {

        Context context = this.context.get();

        if (context != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//            String protocol = prefs.getString(Constants.PREFERENCE_NODE_PROTOCOL, Constants.PREFERENCE_NODE_DEFAULT_PROTOCOL);
//            String host = prefs.getString(Constants.PREFERENCE_NODE_IP, Constants.PREFERENCE_NODE_DEFAULT_IP);
//            int port = Integer.parseInt(prefs.getString(Constants.PREFERENCE_NODE_PORT, Constants.PREFERENCE_NODE_DEFAULT_PORT));
            String protocol = "https";
            String host = Common.NODE_ADDRESS;
//            int port = 80;
            int port = 14265;
//            int port = 443;

            if (BuildConfig.DEBUG) {
                start = System.currentTimeMillis();
                Log.i("ApiRequest", protocol +"://"+ host +":"+ port + " at:" + sdf.format(new Date(start)));
            }

            ApiRequest apiRequest = params[0];
            tag = apiRequest.getClass().getName();

            ApiProvider apiProvider = new IotaApiProvider(protocol, host, port, context);
            ApiResponse apiResponse = apiProvider.processRequest(apiRequest);
            onPostExecuteEx(apiResponse);
            return apiResponse;

        }

        TaskManager.removeTask(tag);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ApiResponse result) {
    }

    protected void onPostExecuteEx(ApiResponse result) {
        if (this.isCancelled()) return;
        if (BuildConfig.DEBUG) {
            if (result != null)
                Log.i("ApiResponse", new Gson().toJson(result));
            Log.i("duration", (System.currentTimeMillis() - start) + " ms");
        }

        if (result != null) {
            bus.post(result);
            if (callback != null)
                callback.onResponse(result);
        } else {
            bus = null;
        }

        TaskManager.removeTask(tag);
    }
}