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

//import org.iota.wallet.R;
import org.iota.wallet.api.requests.ApiRequest;
import org.iota.wallet.api.requests.ReplayBundleRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.api.responses.ReplayBundleResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.api.responses.error.NetworkErrorType;
import org.iota.wallet.helper.NotificationHelper;
import org.iota.wallet.helper.Utils;

import java.util.Arrays;

import jota.IotaAPI;
import jota.error.ArgumentException;

public class ReplayBundleRequestHandler extends IotaRequestHandler {
    public ReplayBundleRequestHandler(IotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return ReplayBundleRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        int notificationId = Utils.createNewID();

//        NotificationHelper.requestNotification(context,
//                R.drawable.ic_replay, context.getString(R.string.notification_replay_bundle_request_title), notificationId);

        try {
            response = new ReplayBundleResponse(apiProxy.replayBundle(((ReplayBundleRequest) request).getHash(),
                    ((ReplayBundleRequest) request).getDepth(),
                    ((ReplayBundleRequest) request).getMinWeightMagnitude()));
        } catch (ArgumentException e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.INVALID_HASH_ERROR);
            response = error;
        }

//        if (response instanceof ReplayBundleResponse && Arrays.asList(((ReplayBundleResponse) response).getSuccessfully()).contains(true))
//            NotificationHelper.responseNotification(context, R.drawable.ic_replay, context.getString(R.string.notification_replay_bundle_response_succeeded_title), notificationId);
//        else if (response instanceof NetworkError)
//            NotificationHelper.responseNotification(context, R.drawable.ic_replay, context.getString(R.string.notification_replay_bundle_response_failed_title), notificationId);
        return response;
    }
}
