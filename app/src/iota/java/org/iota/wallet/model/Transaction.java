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

public class Transaction implements Parcelable {

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
    private String hash;
    private String signatureFragments;
    private String address;
    private long value;
    private String tag;
    private String obsoleteTag;
    private long timestamp;
    private long attachmentTimestamp;
    private long attachmentTimestampLowerBound;
    private long attachmentTimestampUpperBound;
    private long currentIndex;
    private long lastIndex;
    private String bundle;
    private String trunkTransaction;
    private String branchTransaction;
    private String nonce;
    private Boolean persistence;

    public Transaction(String hash, String signatureFragments, String address, long value, String tag, String obsoleteTag, long timestamp,
                       long attachmentTimestamp, long attachmentTimestampLowerBound, long attachmentTimestampUpperBound, long currentIndex,
                       long lastIndex, String bundle, String trunkTransaction, String branchTransaction, String nonce, Boolean persistence) {

        this.hash = hash;
        this.signatureFragments = signatureFragments;
        this.address = address;
        this.value = value;
        this.tag = tag;
        this.obsoleteTag = obsoleteTag;
        this.timestamp = timestamp;
        this.attachmentTimestamp = attachmentTimestamp;
        this.attachmentTimestampLowerBound = attachmentTimestampLowerBound;
        this.attachmentTimestampUpperBound = attachmentTimestampUpperBound;
        this.currentIndex = currentIndex;
        this.lastIndex = lastIndex;
        this.bundle = bundle;
        this.trunkTransaction = trunkTransaction;
        this.branchTransaction = branchTransaction;
        this.nonce = nonce;
        this.persistence = persistence;
    }

    public Transaction(String address, long value, String tag, long timestamp) {
        this.address = address;
        this.value = value;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    public Transaction(Parcel in) {
        hash = in.readString();
        signatureFragments = in.readString();
        address = in.readString();
        value = in.readLong();
        tag = in.readString();
        obsoleteTag = in.readString();
        timestamp = in.readLong();
        attachmentTimestamp = in.readLong();
        attachmentTimestampLowerBound = in.readLong();
        attachmentTimestampUpperBound = in.readLong();
        currentIndex = in.readLong();
        lastIndex = in.readLong();
        bundle = in.readString();
        trunkTransaction = in.readString();
        branchTransaction = in.readString();
        nonce = in.readString();
        persistence = in.readInt() != 0;
    }

    public long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(long lastIndex) {
        this.lastIndex = lastIndex;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignatureFragments() {
        return signatureFragments;
    }

    public void setSignatureFragments(String signatureFragments) {
        this.signatureFragments = signatureFragments;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getObsoleteTag() {
        return obsoleteTag;
    }

    public void setObsoleteTag(String obsoleteTag) {
        this.obsoleteTag = obsoleteTag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAttachmentTimestamp() {
        return attachmentTimestamp;
    }

    public void setAttachmentTimestamp(long attachmentTimestamp) {
        this.attachmentTimestamp = attachmentTimestamp;
    }

    public long getAttachmentTimestampLowerBound() {
        return attachmentTimestampLowerBound;
    }

    public void setAttachmentTimestampLowerBound(long attachmentTimestampLowerBound) {
        this.attachmentTimestampLowerBound = attachmentTimestampLowerBound;
    }

    public long getAttachmentTimestampUpperBound() {
        return attachmentTimestampUpperBound;
    }

    public void setAttachmentTimestampUpperBound(long attachmentTimestampUpperBound) {
        this.attachmentTimestampUpperBound = attachmentTimestampUpperBound;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentIndex) {
        this.currentIndex = currentIndex;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getTrunkTransaction() {
        return trunkTransaction;
    }

    public void setTrunkTransaction(String trunkTransaction) {
        this.trunkTransaction = trunkTransaction;
    }

    public String getBranchTransaction() {
        return branchTransaction;
    }

    public void setBranchTransaction(String branchTransaction) {
        this.branchTransaction = branchTransaction;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Boolean getPersistence() {
        return persistence;
    }

    public void setPersistence(Boolean persistence) {
        this.persistence = persistence;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hash);
        dest.writeString(signatureFragments);
        dest.writeString(address);
        dest.writeLong(value);
        dest.writeString(tag);
        dest.writeString(obsoleteTag);
        dest.writeLong(timestamp);
        dest.writeLong(attachmentTimestamp);
        dest.writeLong(attachmentTimestampLowerBound);
        dest.writeLong(attachmentTimestampUpperBound);
        dest.writeLong(currentIndex);
        dest.writeLong(lastIndex);
        dest.writeString(bundle);
        dest.writeString(trunkTransaction);
        dest.writeString(branchTransaction);
        dest.writeString(nonce);
        dest.writeInt((persistence != null ? persistence : false) ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}