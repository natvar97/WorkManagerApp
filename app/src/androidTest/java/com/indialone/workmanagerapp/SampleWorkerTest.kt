package com.indialone.workmanagerapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.indialone.workmanagerapp.utils.Constants

class SampleWorkerTest(
    private val context: Context,
    private val workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return when(inputData.getString("Worker")) {
            "sampleWork" -> Result.success()
            else -> Result.retry()
        }
    }
}