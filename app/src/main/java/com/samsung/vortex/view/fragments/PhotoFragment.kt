package com.samsung.vortex.view.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.samsung.vortex.R
import com.samsung.vortex.databinding.FragmentPhotoBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Objects

class PhotoFragment : Fragment() {
    private lateinit var binding: FragmentPhotoBinding
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var processCameraProvider: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var camera: Camera

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener({
            cameraProvider = processCameraProvider.get()
            startCameraX(cameraProvider, cameraFacing, flashMode)
        }, ContextCompat.getMainExecutor(requireContext()))
        handleItemClick()
        return binding.root
    }

    private fun handleItemClick() {
        binding.closeCamera.setOnClickListener { requireActivity().finish() }
        binding.takeImage.setOnClickListener { capturePhoto() }
        binding.flipCamera.setOnClickListener { flipCamera() }
        binding.toggleFlash.setOnClickListener { toggleFlash() }
    }

    private fun toggleFlash() {
        when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> {
                flashMode = ImageCapture.FLASH_MODE_ON
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_on_24)
            }

            ImageCapture.FLASH_MODE_ON -> {
                flashMode = ImageCapture.FLASH_MODE_AUTO
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_auto_24)
            }

            ImageCapture.FLASH_MODE_AUTO -> {
                flashMode = ImageCapture.FLASH_MODE_OFF
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_off_24)
            }
        }
        startCameraX(cameraProvider, cameraFacing, flashMode)
    }

    private fun flipCamera() {
        cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCameraX(cameraProvider, cameraFacing, flashMode)
    }

    private fun startCameraX(
        cameraProvider: ProcessCameraProvider?,
        cameraFacing: Int,
        flashMode: Int
    ) {
        cameraProvider?.unbindAll()

        //Camera selector use case
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraFacing)
            .build()

        //Preview use case
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.preview.surfaceProvider)

        //Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetResolution(Size(800, 800))
            .setFlashMode(flashMode)
            .build()
        camera = cameraProvider!!.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    private fun capturePhoto() {
        val name = SimpleDateFormat("yyy-MM-dd-HH-ss-SSS", Locale.US).format(
            System.currentTimeMillis()
        )
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Pictures")
        val outputFileOptions = OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            .build()
        imageCapture.takePicture(outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: OutputFileResults) {
                    val data = Intent()
                    data.putExtra(
                        requireContext().getString(R.string.IMAGE_URI),
                        Objects.requireNonNull<Uri?>(outputFileResults.savedUri).toString()
                    )
                    data.putExtra(
                        requireContext().getString(R.string.FILE_TYPE),
                        requireContext().getString(R.string.IMAGE)
                    )
                    requireActivity().setResult(Activity.RESULT_OK, data)
                    requireActivity().finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        requireContext(),
                        "Error while saving the image : " + exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}