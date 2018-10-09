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

import android.os.Parcel;
import android.os.Parcelable;

import jota.dto.response.AbstractResponse;

public class CoolDetailsResponse extends AbstractResponse implements Parcelable {

    public final static Creator<CoolDetailsResponse> CREATOR = new Creator<CoolDetailsResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public CoolDetailsResponse createFromParcel(Parcel in) {
            CoolDetailsResponse instance = new CoolDetailsResponse();
            instance.hash = ((String) in.readValue((String.class.getClassLoader())));
            instance.addr = ((String) in.readValue((String.class.getClassLoader())));
            instance.bund = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public CoolDetailsResponse[] newArray(int size) {
            return (new CoolDetailsResponse[size]);
        }

    };
    private String hash;
    private String addr;
    private String bund;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getBund() {
        return bund;
    }

    public void setBund(String bund) {
        this.bund = bund;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(hash);
        dest.writeValue(addr);
        dest.writeValue(bund);
    }

    public int describeContents() {
        return 0;
    }

}