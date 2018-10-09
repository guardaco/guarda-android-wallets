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

import org.iota.wallet.api.requests.ApiRequest;
import org.iota.wallet.api.requests.GetBundleRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.api.responses.GetBundleResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.api.responses.error.NetworkErrorType;

import jota.IotaAPI;
import jota.error.ArgumentException;

public class GetBundleRequestHandler extends IotaRequestHandler {
    public GetBundleRequestHandler(IotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetBundleRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        try {
            response =  new GetBundleResponse(apiProxy.getBundle(((GetBundleRequest) request).getTransaction()));
        } catch (ArgumentException e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.INVALID_HASH_ERROR);
            response = error;
        }
        return response;
    }
}
