package com.ibrahimcanerdogan.androidworkmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception

class FilteringWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        try {

            for (i in 0..300) {
                Log.i("MYTAG", "Filtering $i")
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}