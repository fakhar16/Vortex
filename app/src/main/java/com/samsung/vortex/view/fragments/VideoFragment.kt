package com.samsung.vortex.view.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.samsung.vortex.R
import com.samsung.vortex.databinding.FragmentVideoBinding
import java.text.SimpleDateFormat
import java.util.Locale

class VideoFragment : Fragment() {
    private lateinit var binding: FragmentVideoBinding
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var processCameraProvider: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var recording: Recording? = null
    private lateinit var countDownTimer: CountDownTimer
    var counter: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener({
            cameraProvider = processCameraProvider.get()
            startCameraX(cameraProvider, cameraFacing)
        }, ContextCompat.getMainExecutor(requireContext()))
        handleItemClick()
        return binding.root
    }

    private fun handleItemClick() {
        binding.closeCamera.setOnClickListener { requireActivity().finish() }
        binding.takeVideo.setOnClickListener { captureVideo() }
        binding.flipCamera.setOnClickListener { flipCamera() }
    }

    private fun flipCamera() {
        cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCameraX(cameraProvider, cameraFacing)
    }

    private fun startCameraX(cameraProvider: ProcessCameraProvider?, cameraFacing: Int) {
        cameraProvider!!.unbindAll()

        //Camera selector use case
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraFacing)
            .build()

        //Preview use case
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.preview.surfaceProvider)

        //Video capture use case
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            preview,
            videoCapture
        )
    }

    private fun captureVideo() {
        val recording1 = recording
        if (recording1 != null) {
            recording1.stop()
            recording = null
            return
        }
        binding.takeVideo.setImageResource(R.drawable.baseline_stop_circle_24)
        val name = SimpleDateFormat("yyy-MM-dd-HH-ss-SSS", Locale.US).format(
            System.currentTimeMillis()
        )
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Videos")
        val options = MediaStoreOutputOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        recording = videoCapture.output
            .prepareRecording(requireContext(), options).withAudioEnabled()
            .start(
                ContextCompat.getMainExecutor(requireContext())
            ) { videoRecordEvent: VideoRecordEvent? ->
                if (videoRecordEvent is VideoRecordEvent.Start) {
                    startVideo()
                } else if (videoRecordEvent is Finalize) {
                    if (!videoRecordEvent.hasError()) {
                        val data = Intent()
                        data.putExtra(
                            requireContext().getString(R.string.VIDEO_URI),
                            videoRecordEvent.outputResults.outputUri
                                .toString()
                        )
                        data.putExtra(
                            requireContext().getString(R.string.FILE_TYPE),
                            requireContext().getString(R.string.VIDEO)
                        )
                        requireActivity().setResult(Activity.RESULT_OK, data)
                        requireActivity().finish()
                    } else {
                        recording!!.close()
                        recording = null
                        Toast.makeText(
                            requireContext(),
                            "Error while saving the video",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    stopVideo()
                }
            }
    }

    private fun stopVideo() {
        binding.takeVideo.setImageResource(R.drawable.baseline_fiber_manual_record_24)
        binding.flipCamera.visibility = View.VISIBLE
        binding.gallery.visibility = View.VISIBLE
        binding.closeCamera.visibility = View.VISIBLE
        countDownTimer.cancel()
    }

    private fun startVideo() {
        binding.takeVideo.setImageResource(R.drawable.baseline_stop_circle_24)
        binding.flipCamera.visibility = View.GONE
        binding.gallery.visibility = View.GONE
        binding.closeCamera.visibility = View.GONE
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter++
                updateRecordTimerText(counter)
            }

            override fun onFinish() {
                counter = 0
                updateRecordTimerText(counter)
            }
        }.start()
    }

    private fun updateRecordTimerText(millis: Long) {
        val minutes = millis.toInt() / 60
        val seconds = millis.toInt() % 60
        val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
        binding.recordingTimer.text = timeFormatted
    }
}