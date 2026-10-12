package com.studio1111.giyahnameh.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class ImageEnrichmentWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val dao = PlantDatabase.get(applicationContext).plantDao()
        val repository = PlantRepository(dao, applicationContext)
        repository.seedIfNeeded()
        repository.refreshMissingImages()
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        private val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedule(context: Context) {
            val manager = WorkManager.getInstance(context)
            val initial = OneTimeWorkRequestBuilder<ImageEnrichmentWorker>()
                .setConstraints(networkConstraint)
                .build()
            manager.enqueueUniqueWork("giyahnameh-image-enrichment", ExistingWorkPolicy.KEEP, initial)

            val periodic = PeriodicWorkRequestBuilder<ImageEnrichmentWorker>(6, TimeUnit.HOURS)
                .setConstraints(networkConstraint)
                .build()
            manager.enqueueUniquePeriodicWork(
                "giyahnameh-image-refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                periodic
            )
        }
    }
}
