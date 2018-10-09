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

public class RemoveNeighborsResponse extends ApiResponse {

    private Integer removedNeighbors;

    public RemoveNeighborsResponse(jota.dto.response.RemoveNeighborsResponse apiResponse) {
        removedNeighbors = apiResponse.getRemovedNeighbors();
        setDuration(apiResponse.getDuration());
    }

    public Integer getRemovedNeighbors() {
        return removedNeighbors;
    }

    public void setRemovedNeighbors(Integer removedNeighbors) {
        this.removedNeighbors = removedNeighbors;
    }
}
