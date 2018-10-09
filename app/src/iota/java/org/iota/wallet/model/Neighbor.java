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

public class Neighbor implements Parcelable {

    public static final Creator<Neighbor> CREATOR = new Creator<Neighbor>() {
        @Override
        public Neighbor createFromParcel(Parcel in) {
            return new Neighbor(in);
        }

        @Override
        public Neighbor[] newArray(int size) {
            return new Neighbor[size];
        }
    };

    private String address;
    private Integer numberOfAllTransactions;
    private Integer numberOfInvalidTransactions;
    private Integer numberOfNewTransactions;
    private Integer numberOfRandomTransactionRequests;
    private Integer numberOfSentTransactions;
    private String connectionType;
    private Boolean online = false;

    public Neighbor() {
    }

    private Neighbor(Parcel in) {
        address = in.readString();
        numberOfAllTransactions = in.readInt();
        numberOfInvalidTransactions = in.readInt();
        numberOfNewTransactions = in.readInt();
        numberOfRandomTransactionRequests = in.readInt();
        numberOfSentTransactions = in.readInt();
        connectionType = in.readString();
        online = in.readInt() != 0;

    }

    public Neighbor(String ipAddress) {
        setAddress(ipAddress);
    }

    public Neighbor(String address, Integer numberOfAllTransactions, Integer numberOfInvalidTransactions, Integer numberOfNewTransactions, Integer numberOfRandomTransactionRequests, Integer numberOfSentTransactions, String connectionType) {
        this.address = address;
        this.numberOfAllTransactions = numberOfAllTransactions;
        this.numberOfInvalidTransactions = numberOfInvalidTransactions;
        this.numberOfNewTransactions = numberOfNewTransactions;
        this.numberOfRandomTransactionRequests = numberOfRandomTransactionRequests;
        this.numberOfSentTransactions = numberOfSentTransactions;
        this.connectionType = connectionType;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt(numberOfAllTransactions);
        dest.writeInt(numberOfInvalidTransactions);
        dest.writeInt(numberOfNewTransactions);
        dest.writeInt(numberOfRandomTransactionRequests);
        dest.writeInt(numberOfSentTransactions);
        dest.writeString(connectionType);
        dest.writeInt((online != null ? online : false) ? 1 : 0);
    }

    public Boolean isOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    public Integer getNumberOfAllTransactions() {
        return numberOfAllTransactions;
    }

    public void setNumberOfAllTransactions(Integer numberOfAllTransactions) {
        this.numberOfAllTransactions = numberOfAllTransactions;
    }

    public Integer getNumberOfInvalidTransactions() {
        return numberOfInvalidTransactions;
    }

    public void setNumberOfInvalidTransactions(Integer numberOfInvalidTransactions) {
        this.numberOfInvalidTransactions = numberOfInvalidTransactions;
    }

    public Integer getNumberOfNewTransactions() {
        return numberOfNewTransactions;
    }

    public void setNumberOfNewTransactions(Integer numberOfNewTransactions) {
        this.numberOfNewTransactions = numberOfNewTransactions;
    }

    public Integer getNumberOfRandomTransactionRequests() {
        return numberOfRandomTransactionRequests;
    }

    public void setNumberOfRandomTransactionRequests(Integer numberOfRandomTransactionRequests) {
        this.numberOfRandomTransactionRequests = numberOfRandomTransactionRequests;
    }

    public Integer getNumberOfSentTransactions() {
        return numberOfSentTransactions;
    }

    public void setNumberOfSentTransactions(Integer numberOfSentTransactions) {
        this.numberOfSentTransactions = numberOfSentTransactions;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Boolean getOnline() {
        return online;
    }
}
