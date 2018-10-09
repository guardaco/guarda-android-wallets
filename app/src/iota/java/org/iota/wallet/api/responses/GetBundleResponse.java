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

import org.iota.wallet.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetBundleResponse extends ApiResponse {

    private List<Transaction> transactions = new ArrayList<>();

    public GetBundleResponse(jota.dto.response.GetBundleResponse apiResponse) {

        for (jota.model.Transaction transaction : apiResponse.getTransactions()) {

            String hash = transaction.getHash();
            String signatureFragments = transaction.getSignatureFragments();
            String address = transaction.getAddress();
            long value = transaction.getValue();
            String tag = transaction.getTag();
            String obsoleteTag = transaction.getObsoleteTag();
            long timestamp = transaction.getTimestamp();
            long attachmentTimestamp = transaction.getAttachmentTimestamp();
            long attachmentTimestampLowerBound = transaction.getAttachmentTimestampLowerBound();
            long attachmentTimestampUpperBound = transaction.getAttachmentTimestampUpperBound();
            long currentIndex = transaction.getCurrentIndex();
            long lastIndex = transaction.getLastIndex();
            String bundle = transaction.getBundle();
            String trunkTransaction = transaction.getTrunkTransaction();
            String branchTransaction = transaction.getBranchTransaction();
            String nonce = transaction.getNonce();
            Boolean persistence = transaction.getPersistence();

            transactions.add(new Transaction(hash, signatureFragments, address, value, tag, obsoleteTag, timestamp, attachmentTimestamp, attachmentTimestampLowerBound, attachmentTimestampUpperBound, currentIndex, lastIndex, bundle, trunkTransaction, branchTransaction, nonce, persistence));
        }
        Collections.reverse(transactions);

        setDuration(apiResponse.getDuration());

    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}