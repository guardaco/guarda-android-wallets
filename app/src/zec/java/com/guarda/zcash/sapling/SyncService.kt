package com.guarda.zcash.sapling

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.guarda.ethereum.GuardaApp
import com.guarda.ethereum.R
import com.guarda.zcash.sapling.SyncProgress.Companion.SYNCED_PHASE
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class SyncService : Service() {

    @Inject lateinit var syncManager: SyncManager

    private lateinit var notificationManager : NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        GuardaApp.appComponent.inject(this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = buildNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subscribeSync()
        syncManager.startSync()

        notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

        return START_STICKY
    }

    private fun subscribeSync() {
        compositeDisposable.add(
                syncManager.syncProgress.subscribe {
                    if (it.processPhase == SYNCED_PHASE) {
                        stopSelf()
                        notificationManager.cancel(SYNC_NOTIFICATION_ID)
                    } else {
                        notificationBuilder.setContentText("${it.currentBlock} from ${it.toBlock}")
                        notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())
                    }
                }
        )
    }

    private fun buildNotification() : NotificationCompat.Builder {
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(applicationContext, getString(R.string.sync_push_channel_id))
                .setContentTitle(getString(R.string.sync_push_title))
                .setAutoCancel(true)

        val typeDraw: Int = R.mipmap.ic_launcher

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notificationBuilder
                    .setLargeIcon(BitmapFactory.decodeResource(resources, typeDraw))
                    .setSmallIcon(R.mipmap.ic_launcher).color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder
                    .setLargeIcon(BitmapFactory.decodeResource(resources, typeDraw))
                    .setSmallIcon(R.mipmap.ic_launcher).color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
        }

        return notificationBuilder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = getString(R.string.sync_push_channel_id)
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance)

            val description = getString(R.string.sync_push_fallback_title)
            channel.description = description

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        syncManager.stopSync()
        compositeDisposable.clear()
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }

    override fun onBind(intent: Intent) = null

    companion object {
        const val SYNC_NOTIFICATION_ID = 1
    }
}
