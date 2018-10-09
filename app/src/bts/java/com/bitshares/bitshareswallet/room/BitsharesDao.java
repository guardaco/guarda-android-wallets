package com.bitshares.bitshareswallet.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by lorne on 31/10/2017.
 */

@Dao
public interface BitsharesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBlance(List<BitsharesAsset> bitsharesAssetList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMarketTicker(List<BitsharesMarketTicker> bitsharesMarketTickerList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAssetObject(List<BitsharesAssetObject> bitsharesAssetObjectList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistoryObject(List<BitsharesOperationHistory> bitsharesOperationHistoryList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccountObject(List<BitsharesAccountObject> bitsharesAccountObjectList);

    @Query("select * from balance where type = 0 and currency = :strCurrency")
    LiveData<BitsharesAsset> queryTargetAvalaliableBalance(String strCurrency);

    @Query("select * from balance where type = 1")
    List<BitsharesAsset> queryOrderBalance();

    @Delete
    void deleteBalance(List<BitsharesAsset> bitsharesAssetList);

    @Delete
    void deleteOperationHistory(List<BitsharesOperationHistory> bitsharesOperationHistoryList);

    @Query("select balance.id as id, balance.currency as quote, ticker.base as base, " +
            "sum(balance.amount) as amount, sum(balance.amount * ticker.latest * BTS.precision / balance.precision) as total, balance.precision as quote_precision, " +
            "BTS.precision as base_precision, sum(balance.amount * ticker.latest / balance.precision * CURRENCY.precision * currency_ticker.latest) as balance, " +
            "CURRENCY.precision as currency_precision, currency_ticker.base as currency from balance " +
            "inner join (select * from market_ticker) as ticker on balance.currency = ticker.quote and ticker.base = 'BTS'" +
            "inner join (select * from asset_object where symbol = 'BTS') as BTS " +
            "inner join (select * from market_ticker) as currency_ticker on currency_ticker.quote = 'BTS' and currency_ticker.base = :currency " +
            "inner join (select * from asset_object where symbol = :currency) as CURRENCY on currency_ticker.base = CURRENCY.symbol group by balance.currency ")
    LiveData<List<BitsharesBalanceAsset>> queryBalance(String currency);

    @Query("select balance.id as id, balance.currency as quote, ticker.base as base, " +
            "sum(balance.amount) as amount, sum(balance.amount * ticker.latest * BTS.precision / balance.precision) as total, balance.precision as quote_precision, " +
            "BTS.precision as base_precision, sum(balance.amount * ticker.latest / balance.precision * CURRENCY.precision * currency_ticker.latest) as balance, " +
            "CURRENCY.precision as currency_precision, currency_ticker.base as currency from balance " +
            "inner join (select * from market_ticker) as ticker on balance.currency = ticker.quote and ticker.base = 'BTS'" +
            "inner join (select * from asset_object where symbol = 'BTS') as BTS " +
            "inner join (select * from market_ticker) as currency_ticker on currency_ticker.quote = 'BTS' and currency_ticker.base = :currency " +
            "inner join (select * from asset_object where symbol = :currency) as CURRENCY on currency_ticker.base = CURRENCY.symbol and type = 0 group by balance.currency ")
    LiveData<List<BitsharesBalanceAsset>> queryAvaliableBalances(String currency);

    @Query("select * from operation_history order by timestamp desc")
    LiveData<List<BitsharesOperationHistory>> queryOperationHistory();

    @Query("select history_id from operation_history order by timestamp desc limit 1")
    String queryOperationHistoryLatestId();

    @Query("select * from account_object")
    LiveData<List<BitsharesAccountObject>> queryAccountObject();

    @Query("select * from asset_object")
    LiveData<List<BitsharesAssetObject>> queryAssetObjectData();

    @Query("select * from asset_object where symbol in (:symbolArray)")
    List<BitsharesAssetObject> queryAssetObject(String[] symbolArray);

    @Query("select * from asset_object")
    List<BitsharesAssetObject> queryAssetObject();

    @Query("select * from balance")
    List<BitsharesAsset> queryBalanceList();

    @Query("select * from operation_history")
    List<BitsharesOperationHistory> queryOperationHistoryList();

    @Query("select * from asset_object where asset_id = :strAssetId ")
    BitsharesAssetObject queryAssetObjectById(String strAssetId);

    @Query("select * from market_ticker")
    LiveData<List<BitsharesMarketTicker>> queryMarketTicker();

    @Query("select * from market_ticker where base = :base and quote = :quote")
    LiveData<BitsharesMarketTicker> queryMarketTicker(String base, String quote);


}
