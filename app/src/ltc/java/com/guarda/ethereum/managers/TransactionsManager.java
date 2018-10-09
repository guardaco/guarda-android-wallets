package com.guarda.ethereum.managers;

import android.util.Log;

import com.guarda.ethereum.models.items.BtgTxResponse;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.Vout;

import org.bitcoinj.core.Coin;

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


    public List<TransactionItem> transformTxToFriendlyNew(List<BtgTxResponse> transactions, String ownAddress) {
        txFriendlyList.clear();
        for (BtgTxResponse tx : transactions) {
            TransactionItem item = new TransactionItem();
            item.setHash(tx.getHash());
            item.setTime(tx.getTime());
            long sumTx = calculateTxNew(tx, ownAddress);
            item.setOut(getIsOut(tx, ownAddress));
            item.setFrom(getSenderAddress(item.isOut(), tx, ownAddress));
            item.setTo(getReceiverAddress(item.isOut(), tx, ownAddress));
            item.setSum(sumTx);
            item.setReceived(sumTx < 0);
            item.setConfirmations(tx.getConfirmations());
            txFriendlyList.add(item);
        }
        return txFriendlyList;
    }

    private boolean getIsOut(BtgTxResponse item, String ownAddress) {
        if (item.getVin().get(0).getAddr() == null) {
            return false;
        } else {
            return item.getVin().get(0).getAddr().equals(ownAddress);
        }
    }

    private String getSenderAddress(boolean isOutTx, BtgTxResponse tx, String ownAddress) {
        if (isOutTx) {
            return ownAddress;
        } else {
            return tx.getVin().get(0).getAddr();
        }
    }

    private String getReceiverAddress(boolean isOutTx, BtgTxResponse tx, String ownAddress) {
        if (isOutTx) {
            for (Vout out : tx.getVout()) {
                if (!out.getScriptPubKey().getAddresses().get(0).equals(ownAddress))
                    return out.getScriptPubKey().getAddresses().get(0);
            }
        }

        return ownAddress;
    }

    private long calculateTxNew(BtgTxResponse item, String ownAddress) {
        long txSum;

        if (item.getVin().get(0).getAddr() != null) {
            if (item.getVin().get(0).getAddr().equals(ownAddress)) {
                txSum = getOutsSumNew(item, ownAddress);
//            txSum *= -1;
            } else {
                txSum = getInputsSumNew(item, ownAddress);
            }
        } else {
            // for transactions without Vin address (first tx in block - for miner)
            txSum = convertStrCoinToSatoshi(item.getVout().get(0).getValue());
        }

        return txSum;
    }

    private long getInputsSumNew(BtgTxResponse item, String ownAddress) {
        long res = 0;
        for (Vout out : item.getVout()) {
            if (out.getScriptPubKey().getAddresses() != null) {
                if (out.getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                    res += convertStrCoinToSatoshi(out.getValue());
                }
            }
        }
        return res;
    }

    private long getOutsSumNew(BtgTxResponse item, String ownAddress) {
        long res = 0;
        for (Vout out : item.getVout()) {
            // miner's tx has out without scriptPubKey.address
            if (out.getScriptPubKey().getAddresses() == null) continue;
            if (!out.getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                res += convertStrCoinToSatoshi(out.getValue());
            }
        }
        return res;
    }

    private long convertStrCoinToSatoshi(String value) {
        return Coin.parseCoin(value).getValue();
    }

    public long getBalanceByTime(boolean balanceBefore, long curBalanceSatoshi, long time) {
        long res = curBalanceSatoshi;
        for (TransactionItem item : txFriendlyList) {
            if (balanceBefore) {
                if (item.getTime() >= time) {
                    if (item.isOut()) {
                        res += item.getValue();
                    } else {
                        res -= item.getValue();
                    }
                }
            } else {
                if (item.getTime() > time) {
                    if (item.isOut()) {
                        res += item.getValue();
                    } else {
                        res -= item.getValue();
                    }
                }
            }
        }
        if (res < 0) return 0;
        return res;
    }



}
