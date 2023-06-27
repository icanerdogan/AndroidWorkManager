package com.ibrahimcanerdogan.androidworkmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ibrahimcanerdogan.androidworkmanager.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    // Less API 23: Broadcast Receiver and Alarm Manager
    // Higher API 23: JobScheduler

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStart.setOnClickListener {
            // setOneTimeWorkRequest()
            setPeriodicWorkRequest()
        }
    }

    private fun setOneTimeWorkRequest() {
        val instance = WorkManager.getInstance(applicationContext)

        // When battery charging start work manager.
        val constraints = Constraints.Builder()
            .setRequiresCharging(true) // Charge mode. Change emulator battery settings menu.
            .setRequiredNetworkType(NetworkType.CONNECTED) // Internet connected mode.
            .build()

        // Input / Output data
        val data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 500)
            .build()

        // Upload Worker
        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        // Filter Worker
        val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java)
            .build()

        // Compress Worker
        val compressingRequest = OneTimeWorkRequest.Builder(CompressingWorker::class.java)
            .build()

        // Download Worker
        val downloadRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
            .build()

        // Parallel Workers
        val parallelWorks = mutableListOf<OneTimeWorkRequest>()
        parallelWorks.add(downloadRequest)
        parallelWorks.add(filteringRequest)

        //.beginWith(filteringRequest)
        instance
            .beginWith(parallelWorks)
            .then(compressingRequest)
            .then(uploadRequest)
            .enqueue()

        instance.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                binding.textViewState.text = it.state.name
                if (it.state.isFinished) {
                    // Set worker output class name.
                    val outputData = it.outputData
                    val message = outputData.getString(UploadWorker.KEY_WORKER)
                    binding.textViewFinishDate.text = message
                }
            })

        // State
        // BLOCKED: The work is blocked in a chain of works
        // ENQUEUED: As soon as the work is next in the chain of work an eligible to run.
        // RUNNING: When a work is actively executing it is called running.
        // SUCCEEDED: After a work successfully completed, work enters to succeeded state.
    }

    private fun setPeriodicWorkRequest() {
        // Minimum time is 15 minutes.
        val periodicWorkRequest = PeriodicWorkRequest.Builder(DownloadWorker::class.java, 16, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueue(periodicWorkRequest)
    }
    companion object {
        const val KEY_COUNT_VALUE = "key_count"
    }
}