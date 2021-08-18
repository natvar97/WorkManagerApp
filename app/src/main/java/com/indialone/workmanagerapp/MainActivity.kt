package com.indialone.workmanagerapp

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.*
import com.indialone.workmanagerapp.databinding.ActivityMainBinding
import com.indialone.workmanagerapp.utils.Constants
import com.indialone.workmanagerapp.worker.ImageDownloadWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(this@MainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.tvWorkInfo.visibility = View.GONE

        requestStoragePermissions()

        mBinding.btnImageDownload.setOnClickListener {
            showLottieAnimation()
            mBinding.downloadLayout.visibility = View.GONE
//            createOneTimeWorkRequest()
//            createPeriodicWorkRequest()
            createDelayedWorkRequest()
        }

        mBinding.btnQueryWork.setOnClickListener {
            queryWorkInfo()
        }

    }

    private fun createDelayedWorkRequest() {
        val imageRequest = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
            .setConstraints(createConstraints())
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag(Constants.IMAGE_WORK)
            .build()

        workManager.enqueueUniqueWork(
            Constants.DELAYED_IMAGE_DOWNLOAD,
            ExistingWorkPolicy.KEEP,
            imageRequest
        )
        observeWork(imageRequest.id)
    }

    private fun createPeriodicWorkRequest() {
        val imageRequest = PeriodicWorkRequestBuilder<ImageDownloadWorker>(15, TimeUnit.MINUTES)
            .setConstraints(createConstraints())
            .addTag(Constants.IMAGE_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.PERIODIC_IMAGE_DOWNLOAD,
            ExistingPeriodicWorkPolicy.KEEP,
            imageRequest
        )
        observeWork(imageRequest.id)
    }

    private fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresStorageNotLow(true)
        .setRequiresBatteryNotLow(true)
        .build()

    private fun createOneTimeWorkRequest() {
        val imageRequest = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
            .setConstraints(createConstraints())
            .addTag(Constants.IMAGE_WORK)
            .build()

        workManager.enqueueUniqueWork(
            Constants.ONE_TIME_IMAGE_DOWNLOAD,
            ExistingWorkPolicy.KEEP,
            imageRequest
        )
        observeWork(imageRequest.id)
    }

    private fun observeWork(id: UUID) {
        workManager.getWorkInfoByIdLiveData(id)
            .observe(this) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    hideLottieAnimation()
                    mBinding.downloadLayout.visibility = View.VISIBLE
                    val imageUri = workInfo.outputData.getString(Constants.IMAGE_URI)
                    if (imageUri != null) {
                        showDownloadedImage(Uri.parse(imageUri))
                    }
                }
            }
    }

    private fun queryWorkInfo() {
        val workQuery = WorkQuery.Builder
            .fromTags(listOf(Constants.IMAGE_WORK))
            .addStates(listOf(WorkInfo.State.SUCCEEDED))
            .addUniqueWorkNames(
                listOf(
                    Constants.ONE_TIME_IMAGE_DOWNLOAD,
                    Constants.PERIODIC_IMAGE_DOWNLOAD,
                    Constants.DELAYED_IMAGE_DOWNLOAD
                )
            ).build()

        workManager.getWorkInfosLiveData(workQuery).observe(this) { workInfo ->
            mBinding.tvWorkInfo.visibility = View.VISIBLE
            mBinding.tvWorkInfo.text = resources
                .getQuantityString(
                    R.plurals.text_work_desc,
                    workInfo.size,
                    workInfo.size
                )
        }

    }

    private fun requestStoragePermissions() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        }

    private fun showLottieAnimation() {
        mBinding.animationView.visibility = View.VISIBLE
        mBinding.animationView.playAnimation()

    }

    private fun hideLottieAnimation() {
        mBinding.animationView.visibility = View.GONE
        mBinding.animationView.cancelAnimation()

    }

    private fun showDownloadedImage(resultUri: Uri?) {
        mBinding.completeLayout.visibility = View.VISIBLE
        mBinding.downloadLayout.visibility = View.GONE
        hideLottieAnimation()
        mBinding.imgDownloaded.setImageURI(resultUri)
    }

}