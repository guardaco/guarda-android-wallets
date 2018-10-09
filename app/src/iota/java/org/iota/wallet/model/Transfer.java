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

package org.iota.wallet.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

public class Transfer implements Parcelable, Comparable<Transfer> {

    public static final Creator<Transfer> CREATOR = new Creator<Transfer>() {
        @Override
        public Transfer createFromParcel(Parcel in) {
            return new Transfer(in);
        }

        @Override
        public Transfer[] newArray(int size) {
            return new Transfer[size];
        }
    };
    private long timestamp;
    private String address;
    private String hash;
    private Boolean persistence;
    private long value;
    private String message;
    private String tag;

    public Transfer(String address, long value, String message, String tag) {
        this.address = address;
        this.value = value;
        this.message = message;
        this.tag = tag;
    }

    public Transfer(long timestamp, String address, String hash, Boolean persistence,
                    long value, String message, String tag) {
        this.timestamp = timestamp;
        this.address = address;
        this.hash = hash;
        this.persistence = persistence;
        this.value = value;
        this.message = message;
        this.tag = tag;
    }

    public Transfer(Parcel in) {
        timestamp = in.readLong();
        address = in.readString();
        hash = in.readString();
        persistence = in.readInt() != 0;
        value = in.readLong();
        message = in.readString();
        tag = in.readString();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(address);
        dest.writeString(hash);
        dest.writeInt((persistence != null ? persistence : false) ? 1 : 0);
        dest.writeLong(value);
        dest.writeString(message);
        dest.writeString(tag);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getPersistence() {
        return persistence;
    }

    public void setPersistence(Boolean persistence) {
        this.persistence = persistence;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(@NonNull Transfer transfer) {
        return Long.compare(transfer.getTimestamp(), getTimestamp());
    }
}
