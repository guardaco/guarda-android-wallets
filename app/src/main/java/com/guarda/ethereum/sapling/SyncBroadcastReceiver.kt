package com.guarda.ethereum.sapling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.guarda.ethereum.models.constants.Extras
import com.guarda.ethereum.models.constants.Extras.STOP_SYNC_SERVICE

class SyncBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.extras?.getString(Extras.FIRST_ACTION_MAIN_ACTIVITY) ?: return

        if (action == STOP_SYNC_SERVICE) {
            context?.stopService(Intent(context, SyncService::class.java)) ?: return
        }
    }
}