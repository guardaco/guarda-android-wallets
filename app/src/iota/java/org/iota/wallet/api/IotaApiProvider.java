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
import androidx.appcompat.preference.PreferenceManager;

import org.iota.wallet.api.handler.AddNeighborsRequestHandler;
import org.iota.wallet.api.handler.CoolTransactionsRequestHandler;
import org.iota.wallet.api.handler.FindTransactionsRequestHandler;
import org.iota.wallet.api.handler.GetAccountDataRequestHandler;
import org.iota.wallet.api.handler.GetBundleRequestHandler;
import org.iota.wallet.api.handler.GetNeighborsRequestHandler;
import org.iota.wallet.api.handler.GetNewAddressRequestHandler;
import org.iota.wallet.api.handler.NodeInfoRequestHandler;
import org.iota.wallet.api.handler.RemoveNeighborsRequestHandler;
import org.iota.wallet.api.handler.ReplayBundleRequestHandler;
import org.iota.wallet.api.handler.RequestHandler;
import org.iota.wallet.api.handler.SendTransferRequestHandler;
import org.iota.wallet.api.requests.ApiRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.api.responses.error.NetworkErrorType;
import org.iota.wallet.helper.Constants;

import java.util.HashMap;
import java.util.Map;

import cfb.pearldiver.PearlDiverLocalPoW;
import jota.IotaAPI;

public class IotaApiProvider implements ApiProvider {
    private final IotaAPI iotaApi;
    private final Context context;
    private Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap;

    public IotaApiProvider(String protocol, String host, int port, Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        if (prefs.getBoolean(Constants.PREFERENCES_LOCAL_POW, false))
            this.iotaApi = new IotaAPI.Builder().localPoW(new PearlDiverLocalPoW()).protocol(protocol).host(host).port(((Integer) port).toString()).build();
        else
            this.iotaApi = new IotaAPI.Builder().protocol(protocol).host(host).port(((Integer) port).toString()).build();

        this.context = context;
        loadRequestMap();
    }

    private void loadRequestMap() {
        Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap = new HashMap<>();

        AddNeighborsRequestHandler addNeighborsAction = new AddNeighborsRequestHandler(iotaApi, context);
        CoolTransactionsRequestHandler coolTransactionsAction = new CoolTransactionsRequestHandler(iotaApi, context);
        FindTransactionsRequestHandler findTransactionsAction = new FindTransactionsRequestHandler(iotaApi, context);
        GetBundleRequestHandler getBundleAction = new GetBundleRequestHandler(iotaApi, context);
        GetNeighborsRequestHandler getNeighborsAction = new GetNeighborsRequestHandler(iotaApi, context);
        GetNewAddressRequestHandler getNewAddressAction = new GetNewAddressRequestHandler(iotaApi, context);
        GetAccountDataRequestHandler getAccountDataAction = new GetAccountDataRequestHandler(iotaApi, context);
        RemoveNeighborsRequestHandler removeNeighborsAction = new RemoveNeighborsRequestHandler(iotaApi, context);
        ReplayBundleRequestHandler replayBundleAction = new ReplayBundleRequestHandler(iotaApi, context);
        SendTransferRequestHandler sendTransferAction = new SendTransferRequestHandler(iotaApi, context);
        NodeInfoRequestHandler nodeInfoAction = new NodeInfoRequestHandler(iotaApi, context);

        requestHandlerMap.put(addNeighborsAction.getType(), addNeighborsAction);
        requestHandlerMap.put(coolTransactionsAction.getType(), coolTransactionsAction);
        requestHandlerMap.put(findTransactionsAction.getType(), findTransactionsAction);
        requestHandlerMap.put(getBundleAction.getType(), getBundleAction);
        requestHandlerMap.put(getNeighborsAction.getType(), getNeighborsAction);
        requestHandlerMap.put(getNewAddressAction.getType(), getNewAddressAction);
        requestHandlerMap.put(getAccountDataAction.getType(), getAccountDataAction);
        requestHandlerMap.put(removeNeighborsAction.getType(), removeNeighborsAction);
        requestHandlerMap.put(replayBundleAction.getType(), replayBundleAction);
        requestHandlerMap.put(sendTransferAction.getType(), sendTransferAction);
        requestHandlerMap.put(nodeInfoAction.getType(), nodeInfoAction);

        this.requestHandlerMap = requestHandlerMap;
    }

    @Override
    public ApiResponse processRequest(ApiRequest apiRequest) {
        ApiResponse response = null;

        try {
            if (this.requestHandlerMap.containsKey(apiRequest.getClass())) {
                RequestHandler requestHandler = this.requestHandlerMap.get(apiRequest.getClass());
                response = requestHandler.handle(apiRequest);
            }
        } catch (IllegalAccessError e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.ACCESS_ERROR);
            response = error;
            e.printStackTrace();
        } catch (Exception e) {
            response = new NetworkError();
        }
        return response == null ? new NetworkError() : response;
    }
}
