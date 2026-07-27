package com.abhik.paisatrack.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncManager {

    private const val ONE_TIME_SYNC_WORK = "paisa_track_one_time_sync"

    fun triggerSyncNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
}
