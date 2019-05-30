package com.guarda.ethereum.managers;

import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.Vjoinsplit;
import com.guarda.ethereum.models.items.Vout;
import com.guarda.ethereum.models.items.ZecTxResponse;


import org.bitcoinj.core.Coin;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
        if (transactionsList != null && transactionsList.size() > 0 && pendingTxHash != null) {
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
        if (pendingTransactions != null && pendingTransactions.size() > 0) {
            for (int i = 0; i < pendingTransactions.size(); i++) {
                String currentTxHash = pendingTransactions.get(i).getHash();
                if (mainListContainsPending(currentTxHash)) {
                    pendingTransactions.remove(i);
                }
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


    public List<TransactionItem> transformTxToFriendlyNew(List<ZecTxResponse> transactions, String ownAddress) {
        txFriendlyList.clear();
        for (ZecTxResponse tx : transactions) {
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

    private boolean getIsOut(ZecTxResponse item, String ownAddress) {
        if (item.getVjoinsplit() == null || item.getVjoinsplit().size() == 0) {
            if (item.getVin().size() == 0) return false;
            if (item.getVin().get(0).getAddr() != null) {
                return item.getVin().get(0).getAddr().equals(ownAddress);
            }
        } else {
            if (item.getVout().size() == 0) {
                return true;
            }
        }

        return false;
    }

    private String getSenderAddress(boolean isOutTx, ZecTxResponse tx, String ownAddress) {
        if (tx.getVjoinsplit() == null) {
            if (isOutTx) {
                return ownAddress;
            } else {
                return tx.getVin().get(0).getAddr();
            }
        } else {
            if (isOutTx) {
                return ownAddress;
            } else {
                return Common.ZCASH_JOIN_SPLIT;
            }
        }
    }

    private String getReceiverAddress(boolean isOutTx, ZecTxResponse tx, String ownAddress) {
        if (tx.getVjoinsplit() == null) {
            if (!isOutTx) {
                return ownAddress;
            } else {
                if (tx.getVout().size() > 0) {
                    if (!tx.getVout().get(0).getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                        return tx.getVout().get(0).getScriptPubKey().getAddresses().get(0);
                    }
                }
            }
        } else {
            if (!isOutTx) {
                return ownAddress;
            } else {
                return Common.ZCASH_JOIN_SPLIT;
            }
        }

        return ownAddress;
    }

    private long calculateTxNew(ZecTxResponse item, String ownAddress) {
        long txSum;

        if (item.getVjoinsplit() == null || item.getVjoinsplit().size() == 0) {
            if (item.getVin().size() == 0) return 0;
            if (item.getVin().get(0).getAddr() != null && item.getVin().get(0).getAddr().equals(ownAddress)) {
                txSum = getOutsSumNew(item, ownAddress);
//            txSum *= -1;
            } else {
                txSum = getInputsSumNew(item, ownAddress);
            }
        } else {
            txSum = getJoinSplitSum(item, ownAddress);
        }

        return txSum;
    }

    private long getJoinSplitSum(ZecTxResponse item, String ownAddress) {
        long res = 0;
        if (item.getVin().size() == 0) {
            res = getInputsSumNew(item, ownAddress);
        } else if (item.getVout().size() == 0) {
            for (Vjoinsplit vj : item.getVjoinsplit()) {
                res += convertStrCoinToSatoshi(vj.getVpub_old());
            }
        }
        return res;
    }

    private long getInputsSumNew(ZecTxResponse item, String ownAddress) {
        long res = 0;
        for (Vout out : item.getVout()) {
            if (out.getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                res += convertStrCoinToSatoshi(out.getValue());
            }
        }
        return res;
    }

    private long getOutsSumNew(ZecTxResponse item, String ownAddress) {
        long res = 0;
        try {
            for (Vout out : item.getVout()) {
                // miner's tx has out without scriptPubKey.address
                if (out.getScriptPubKey().getAddresses() == null) continue;
                if (!out.getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                    res += convertStrCoinToSatoshi(out.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("getOutsSumNew e=%s", e.getMessage());
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
