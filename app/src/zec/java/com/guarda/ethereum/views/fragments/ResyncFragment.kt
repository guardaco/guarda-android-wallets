package com.guarda.ethereum.views.fragments

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.guarda.ethereum.R
import com.guarda.ethereum.lifecycle.ResyncViewModel
import com.guarda.ethereum.rest.RequestorBtc
import com.guarda.ethereum.utils.DateTimeUtil.dateFromTimestamp
import com.guarda.ethereum.utils.IntentUtil.Companion.openWebUrl
import com.guarda.ethereum.views.activity.MainActivity
import com.guarda.ethereum.views.fragments.base.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_resync.*
import timber.log.Timber

class ResyncFragment : BaseFragment() {

    lateinit var resyncViewModel: ResyncViewModel
    private val compositeDisposable = CompositeDisposable()

    override fun getLayout() = R.layout.fragment_resync

    override fun init() {
        val factory = ResyncViewModel.Factory()
        resyncViewModel = ViewModelProviders.of(this, factory).get(ResyncViewModel::class.java)
        initView()
        initSubscribers()
    }

    private fun initView() {
        (activity as MainActivity?)?.setToolBarTitle(R.string.resync_text_title)
        resync_desc.setOnClickListener {
            openWebUrl(activity ?: return@setOnClickListener, FAQ_WEB_URL)
        }

        et_resync_height.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                blockDataByHeight(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        et_resync_height.setText(resyncViewModel.blockHeightPreset().toString())

        btn_resync.setOnClickListener {
            showProgress()
            resyncViewModel.resyncFromHeight(et_resync_height.text.toString().toLongOrNull())
        }
    }

    private fun initSubscribers() {
        resyncViewModel.resync.observe(viewLifecycleOwner, Observer {
            closeProgress()
            (activity as MainActivity?)?.goToTransactionHistory()
        })
    }

    private fun blockDataByHeight(input: String) {
        if (input.length < 6) {
            height_date.text = ""
            return
        }
        val blockHeight = input.toLongOrNull()
        if (blockHeight == null || blockHeight < 500000L) {
            height_date.text = ""
            return
        }

        compositeDisposable.add(
                RequestorBtc.getBlockBookBlock(blockHeight.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { (_, time) ->
                                    height_date.text = String.format(
                                            getString(R.string.restore_sync_date),
                                            dateFromTimestamp(
                                                    time
                                            )
                                    )
                                }
                        ) { error: Throwable ->
                            height_date.text = ""
                            Timber.e("getBlockBookBlock error=%s", error.message)
                        })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    companion object {
        private const val FAQ_WEB_URL = "https://guarda.freshdesk.com/a/solutions/articles/36000247593"
    }
}