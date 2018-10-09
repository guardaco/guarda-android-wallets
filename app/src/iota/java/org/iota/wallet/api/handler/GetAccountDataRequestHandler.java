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
import org.iota.wallet.api.requests.GetAccountDataRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.api.responses.GetAccountDataResponse;
import org.iota.wallet.api.responses.error.NetworkError;

import jota.IotaAPI;
import jota.error.ArgumentException;

public class GetAccountDataRequestHandler extends IotaRequestHandler {
    public GetAccountDataRequestHandler(IotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetAccountDataRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;

        try {
            response = new GetAccountDataResponse(apiProxy.getAccountData(((GetAccountDataRequest) request).getSeed(),
                    ((GetAccountDataRequest) request).getSecurity(),
                    ((GetAccountDataRequest) request).getIndex(),
                    ((GetAccountDataRequest) request).isChecksum(),
                    ((GetAccountDataRequest) request).getTotal(),
                    ((GetAccountDataRequest) request).isReturnAll(),
                    ((GetAccountDataRequest) request).getStart(),
                    ((GetAccountDataRequest) request).getEnd(),
                    ((GetAccountDataRequest) request).isInclusionState(),
                    ((GetAccountDataRequest) request).getThreshold()));
        } catch (ArgumentException e) {
            response = new NetworkError();
        }
        return response;
    }
}
