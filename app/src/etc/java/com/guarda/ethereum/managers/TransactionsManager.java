package com.guarda.ethereum.managers;

import com.guarda.ethereum.models.items.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold transactions received from etherscan.io and raw transactions
 * from {@link com.guarda.ethereum.views.activity.SendingCurrencyActivity}
 * Created by SV on 19.08.2017.
 */

public class TransactionsManager {

    private List<TransactionResponse> transactionsList;
    private List<TransactionResponse> pendingTransactions;

    public TransactionsManager() {
        pendingTransactions = new ArrayList<>();
        transactionsList = new ArrayList<>();
    }

    public void addPendingTransaction(TransactionResponse transaction){
        if (!mainListContainsPending(transaction.getHash())) {
            pendingTransactions.add(0, transaction);
        }
    }

    public List<TransactionResponse> getPendingTransactions(){
        return pendingTransactions;
    }

    private boolean mainListContainsPending(String pendingTxHash) {
        if (transactionsList != null && pendingTxHash != null) {
            for (int i = 0; i < transactionsList.size(); i++) {
                String currentTxHash = transactionsList.get(i).getHash();
                if (pendingTxHash.equals(currentTxHash)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clearDuplicateTransactions() {
        for (int i = 0; i < pendingTransactions.size(); i++) {
            String currentTxHash = pendingTransactions.get(i).getHash();
            if (mainListContainsPending(currentTxHash)) {
                pendingTransactions.remove(i);
            }
        }
    }
    public TransactionResponse getTxByPosition(int position) {
        if (position + 1 <= pendingTransactions.size()) {
            return pendingTransactions.get(position);
        } else {
            if (transactionsList != null && !transactionsList.isEmpty()) {
                return transactionsList.get(position - pendingTransactions.size());
            } else {
                return null;
            }
        }
    }

    public List<TransactionResponse> getTransactionsList() {
        return transactionsList;
    }

    public void setTransactionsList(List<TransactionResponse> mTransactionsList) {
        this.transactionsList = mTransactionsList;
        clearDuplicateTransactions();
    }

}
