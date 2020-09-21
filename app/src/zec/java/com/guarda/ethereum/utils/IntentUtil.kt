package com.guarda.ethereum.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import timber.log.Timber

class IntentUtil {

    companion object {
        fun openAppOrGooglePlayPage(context: Context, packageName: String) {
            val i: Intent
            i = try {
                context.packageManager.getLaunchIntentForPackage(packageName)
                    ?: throw PackageManager.NameNotFoundException()
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.e("openAppOrGooglePlayPage package=$packageName NameNotFoundException error: $e")
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            } catch (e: Exception) {
                Timber.e("openAppOrGooglePlayPage package=$packageName error: $e")
                Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName"))
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }

        fun openGooglePlayPage(context: Context, packageName: String) {
            val i: Intent
            i = try {
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            } catch (e: Exception) {
                Timber.e("openAppOrGooglePlayPage package=$packageName error: $e")
                Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName"))
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }

        fun openWebUrl(context: Context, url: String) {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                context.startActivity(i)
            } catch (e: ActivityNotFoundException) {
                Timber.e("openWebUrl: url=$url error ${e.message}")
            }
        }
    }

}