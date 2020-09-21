package com.guarda.ethereum.views.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.guarda.ethereum.R
import com.guarda.ethereum.lifecycle.ResyncViewModel
import com.guarda.ethereum.utils.IntentUtil.Companion.openWebUrl
import com.guarda.ethereum.views.activity.MainActivity
import com.guarda.ethereum.views.fragments.base.BaseFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_resync.*

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
        (activity as MainActivity?)?.setToolBarTitle(R.string.resync_title)
        ll_faq.setOnClickListener {
            openWebUrl(activity ?: return@setOnClickListener, FAQ_WEB_URL)
        }

        et_resync_height.setText(resyncViewModel.blockHeightPreset().toString())

        btn_resync.setOnClickListener {
            resyncViewModel.resyncFromHeight(et_resync_height.text.toString().toLongOrNull())
        }
    }

    private fun initSubscribers() {
        resyncViewModel.resync.observe(viewLifecycleOwner, Observer {
            (activity as MainActivity?)?.goToTransactionHistory()
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