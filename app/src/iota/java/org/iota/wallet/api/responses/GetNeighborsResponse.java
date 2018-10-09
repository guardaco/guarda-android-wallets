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

package org.iota.wallet.api.responses;

import org.iota.wallet.model.Neighbor;

import java.util.ArrayList;
import java.util.List;

public class GetNeighborsResponse extends ApiResponse {

    private List<Neighbor> neighbors = new ArrayList<>();

    public GetNeighborsResponse(jota.dto.response.GetNeighborsResponse apiResponse) {

        for (jota.model.Neighbor neighbor : apiResponse.getNeighbors()) {
            String address = neighbor.getAddress();
            Integer numberOfAllTransactions = neighbor.getNumberOfAllTransactions();
            Integer numberOfInvalidTransactions = neighbor.getNumberOfInvalidTransactions();
            Integer numberOfNewTransactions = neighbor.getNumberOfNewTransactions();
            Integer numberOfRandomTransactionRequests = neighbor.getNumberOfRandomTransactionRequests();
            Integer numberOfSentTransactions = neighbor.getNumberOfSentTransactions();
            String connectionType = neighbor.getConnectionType();


            neighbors.add(new Neighbor(address, numberOfAllTransactions, numberOfInvalidTransactions, numberOfNewTransactions, numberOfRandomTransactionRequests, numberOfSentTransactions, connectionType));
        }
        setDuration(apiResponse.getDuration());
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

}