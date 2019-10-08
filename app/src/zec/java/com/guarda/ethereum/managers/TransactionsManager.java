package com.guarda.ethereum.managers;

import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.Vin;
import com.guarda.ethereum.models.items.Vjoinsplit;
import com.guarda.ethereum.models.items.Vout;
import com.guarda.ethereum.models.items.ZecTxResponse;


import org.bitcoinj.core.Coin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static com.guarda.zcash.crypto.Utils.roundDouble;

/**
 * Hold transactions received from etherscan.io and raw transactions
 * from {@link com.guarda.ethereum.views.activity.SendingCurrencyActivity}
 * Created by SV on 19.08.2017.
 */

public class TransactionsManager {

    private List<TransactionItem> transactionsList = new ArrayList<>();
    private List<TransactionItem> txFriendlyList = new ArrayList<>();

    public TransactionsManager() {
        transactionsList = new ArrayList<>();
    }

    public TransactionItem getTxByPosition(int position) {
        if (transactionsList != null && !transactionsList.isEmpty()) {
            return transactionsList.get(position);
        } else {
            return null;
        }
    }

    public List<TransactionItem> getTransactionsList() {
        return transactionsList;
    }

    public void setTransactionsList(List<TransactionItem> transactionsList) {
        this.transactionsList = transactionsList;
        //sort transactions with the same hash (self)
        //out transaction should be first
        Collections.sort(this.transactionsList,
                (tx1, tx2) -> {
                    if (!tx1.getHash().equalsIgnoreCase(tx2.getHash())) return 0;

                    if (tx1.isOut() && !tx2.isOut()) return 1;
                    if (!tx1.isOut() && tx2.isOut()) return -1;

                    return 0;
                });
    }

    public void clearLists() {
        transactionsList.clear();
        txFriendlyList.clear();
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
            String inAddr = item.getVin().get(0).getAddr();
            if (inAddr != null) {
                //in case transactions is self
                if (item.getVin().size() > 0 && item.getVout().size() > 0) {
                    boolean isSelfTx = true;
                    for (Vin vin : item.getVin()) {
                        if (!vin.getAddr().equalsIgnoreCase(ownAddress)) {
                            isSelfTx = false;
                        }
                    }
                    for (Vout vout : item.getVout()) {
                        if (!vout.getScriptPubKey().getAddresses().contains(ownAddress)) {
                            isSelfTx = false;
                        }
                    }
                    if (isSelfTx && item.getOutputDescs().size() == 0) return false;
                }
                //other cases
                return inAddr.equals(ownAddress);
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
            //for z to t transaction
            if (item.getVin().size() == 0) {
                //for send all from z to t
                if (item.getOutputDescs().size() == 0 && item.getSpendDescs().size() > 0) {
                    if (item.getVout().get(0).getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                        txSum = convertStrCoinToSatoshi(item.getVout().get(0).getValue());
                        return txSum;
                    }
                }
                //otherwise
                txSum = getOutsSumNew(item, ownAddress);
                return txSum;
            }

            if (item.getVin().get(0).getAddr() != null && item.getVin().get(0).getAddr().equals(ownAddress)) {
                txSum = getOutsSumNew(item, ownAddress);
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
        long resSelf = 0;
        try {
            if (item.getOutputDescs() != null && item.getOutputDescs().size() == 1) {
                double d = item.getValueIn() - item.getValueOut();
                res = Coin.parseCoin(String.valueOf(roundDouble(d, 8))).getValue();
                return (res < 0) ? -res : res;
            }

            for (Vout out : item.getVout()) {
                // miner's tx has out without scriptPubKey.address
                if (out.getScriptPubKey().getAddresses() == null) continue;
                if (!out.getScriptPubKey().getAddresses().get(0).equals(ownAddress)) {
                    res += convertStrCoinToSatoshi(out.getValue());
                } else {
                    resSelf += convertStrCoinToSatoshi(out.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("getOutsSumNew e=%s", e.getMessage());
        }
        if (resSelf != 0) return resSelf;
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
                        res += item.getSum();
                    } else {
                        res -= item.getSum();
                    }
                }
            } else {
                if (item.getTime() > time) {
                    if (item.isOut()) {
                        res += item.getSum();
                    } else {
                        res -= item.getSum();
                    }
                }
            }
        }
        if (res < 0) return 0;
        return res;
    }



}
