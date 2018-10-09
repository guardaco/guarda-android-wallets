/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.iota.wallet.api;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.managers.Callback;

import org.iota.wallet.api.requests.ApiRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskManager {

    private static final HashMap<String, AsyncTask> runningTasks = new HashMap<>();
    private final Context context;

    public TaskManager(Context context) {
        this.context = context;
    }

    private static synchronized void addTask(RequestTask requestTask, ApiRequest ir) {
        String tag = ir.getClass().getName();
        if (!runningTasks.containsKey(tag)) {
            runningTasks.put(tag, requestTask);
            if (BuildConfig.DEBUG)
                Log.i("Added Task ", tag);
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
        }
    }

    public static synchronized void removeTask(String tag) {
        if (runningTasks.containsKey(tag)) {
            if (BuildConfig.DEBUG)
                Log.i("Removed Task ", tag);
            runningTasks.remove(tag);
        }
    }

    public static void stopAndDestroyAllTasks(Context context) {
        Iterator<Map.Entry<String, AsyncTask>> it = runningTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, AsyncTask> entry = it.next();
            try {
                entry.getValue().cancel(true);
            } catch (IllegalStateException e) {
                e.getStackTrace();
            }
            it.remove();
        }

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public void startNewRequestTask(ApiRequest ir, Callback<Object> callback) {
        RequestTask rt = new RequestTask(context, callback);
        TaskManager.addTask(rt, ir);
    }
}
