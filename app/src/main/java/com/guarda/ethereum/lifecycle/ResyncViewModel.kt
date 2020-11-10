package com.guarda.ethereum.lifecycle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.guarda.ethereum.GuardaApp
import com.guarda.ethereum.managers.WalletManager
import com.guarda.ethereum.rxcall.CallCleanDbLogOut
import com.guarda.ethereum.sapling.SyncManager
import com.guarda.ethereum.sapling.db.DbManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ResyncViewModel private constructor() : ViewModel() {

    @Inject lateinit var walletManager: WalletManager
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var dbManager: DbManager

    private val compositeDisposable = CompositeDisposable()
    val resync = MutableLiveData<Boolean>()

    init {
        GuardaApp.getAppComponent().inject(this)
    }

    fun blockHeightPreset() : Long {
        val height = walletManager.createHeight
        return if (height == 0L) {
            SYNC_HEIGHT_PRESET
        } else {
            height
        }
    }

    fun resyncFromHeight(blockHeight: Long?) {
        syncManager.stopSync()

        walletManager.createHeight = if (blockHeight == null || blockHeight == 0L) {
            GUARDA_SHIELDED_FIRST_SYNC_BLOCK
        } else {
            blockHeight
        }

        compositeDisposable.add(
                Observable
                        .fromCallable(CallCleanDbLogOut(dbManager, true))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { latest: Boolean? ->
                            resync.value = true
                            Timber.d("resyncFromHeight cleanDbLogOut done=%s", latest)
                        }
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    class Factory : NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ResyncViewModel() as T
        }
    }

    companion object {
        const val GUARDA_SHIELDED_FIRST_SYNC_BLOCK = 551912L
        const val SYNC_HEIGHT_PRESET = 915000L
    }

}