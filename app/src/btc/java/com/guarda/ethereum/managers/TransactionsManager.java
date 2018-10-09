package com.guarda.ethereum.managers;

import com.guarda.ethereum.models.items.BalanceAndTxResponse;
import com.guarda.ethereum.models.items.TransactionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold transactions received from etherscan.io and raw transactions
 * from {@link com.guarda.ethereum.views.activity.SendingCurrencyActivity}
 * Created by SV on 19.08.2017.
 */

public class TransactionsManager {

    private List<TransactionItem> transactionsList;
    private List<TransactionItem> pendingTransactions;

    private List<TransactionItem> txFriendlyList = new ArrayList<>();

    public TransactionsManager() {
        pendingTransactions = new ArrayList<>();
        transactionsList = new ArrayList<>();
    }

    public void addPendingTransaction(TransactionItem transaction) {
        if (!mainListContainsPending(transaction.getHash())) {
            pendingTransactions.add(0, transaction);
        }
    }

    public List<TransactionItem> getPendingTransactions() {
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

    public TransactionItem getTxByPosition(int position) {
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

    public List<TransactionItem> getTransactionsList() {
        return transactionsList;
    }

    public void setTransactionsList(List<TransactionItem> mTransactionsList) {
        this.transactionsList = mTransactionsList;
        clearDuplicateTransactions();
    }

    public List<TransactionItem> transformTxToFriendly(List<BalanceAndTxResponse.BtcTransaction> transactions, Long currentBlockHeight, String ownAddress) {
        txFriendlyList.clear();
        for (BalanceAndTxResponse.BtcTransaction tx : transactions) {
            TransactionItem item = new TransactionItem();
            item.setHash(tx.getHash());
            item.setTime(tx.getTime());
            long sumTx = calculateTx(tx, ownAddress);
            item.setFrom(getSenderAddress(sumTx < 0, tx, ownAddress));
            item.setTo(getReceiverAddress(sumTx > 0, tx, ownAddress));
            item.setSum(sumTx);
            item.setReceived(sumTx < 0);
            if (currentBlockHeight != null) {
                item.setConfirmations(tx.getBlockHeight() == null ? 0 : currentBlockHeight - tx.getBlockHeight());
            } else {
                item.setConfirmations(-1);
            }
            txFriendlyList.add(item);
        }

        return txFriendlyList;
    }

    private String getSenderAddress(boolean isOutTx, BalanceAndTxResponse.BtcTransaction tx, String ownAddress) {
        if (isOutTx) {
            return ownAddress;
        } else {
            for (BalanceAndTxResponse.Inputs inputs : tx.getInputs())
                if (!inputs.getOut().getAddress().equals(ownAddress))
                    return inputs.getOut().getAddress();
        }
        return "";
    }

    private String getReceiverAddress(boolean isOutTx, BalanceAndTxResponse.BtcTransaction tx, String ownAddress) {
        if (isOutTx) {
            return ownAddress;
        } else {
            for (BalanceAndTxResponse.BtcOutTx out : tx.getOuts())
                if (!out.getAddress().equals(ownAddress))
                    return out.getAddress();
        }
        return ownAddress;
    }

    public List<TransactionItem> getTxFriendly() {
        return txFriendlyList;
    }

    private long calculateTx(BalanceAndTxResponse.BtcTransaction item, String ownAddress) {
        long txSum = 0;
        if (addressContainsInOuts(item, ownAddress) && !addressContainsInInputs(item, ownAddress)) {
            txSum = getOutsSum(item, ownAddress);
        } else if (addressContainsInInputs(item, ownAddress)) {
            txSum = getInputsSum(item, ownAddress) - getOutsSum(item, ownAddress);
            txSum *= -1;
        }

        return txSum;
    }

    private boolean addressContainsInInputs(BalanceAndTxResponse.BtcTransaction item, String ownAddress) {
        boolean res = false;
        for (BalanceAndTxResponse.Inputs input : item.getInputs()) {
            if (input.getOut().getAddress().equals(ownAddress)) {
                res = true;
            }
        }
        return res;
    }

    private boolean addressContainsInOuts(BalanceAndTxResponse.BtcTransaction item, String ownAddress) {
        boolean res = false;
        for (BalanceAndTxResponse.BtcOutTx out : item.getOuts()) {
            if (out.getAddress() == null) continue;
            if (out.getAddress().equals(ownAddress)) {
                res = true;
            }
        }
        return res;
    }

    private long getInputsSum(BalanceAndTxResponse.BtcTransaction item, String ownAddress) {
        int res = 0;
        for (BalanceAndTxResponse.Inputs input : item.getInputs()) {
            if (input.getOut().getAddress().equals(ownAddress)) {
                res += input.getOut().getValue();
            }
        }
        return res;
    }

    private long getOutsSum(BalanceAndTxResponse.BtcTransaction item, String ownAddress) {
        int res = 0;
        for (BalanceAndTxResponse.BtcOutTx out : item.getOuts()) {
            if (out.getAddress().equals(ownAddress)) {
                res += out.getValue();
            }
        }
        return res;
    }

    public long getBalanceByTime(boolean balanceBefore, long curBalanceSatoshi, long time) {
        long res = curBalanceSatoshi;
        for (TransactionItem item : txFriendlyList) {
            if (balanceBefore) {
                if (item.getTime() >= time) {
                    res -= item.getValue();
                }
            } else {
                if (item.getTime() > time) {
                    res -= item.getValue();
                }
            }
        }

        return res;
    }

}
