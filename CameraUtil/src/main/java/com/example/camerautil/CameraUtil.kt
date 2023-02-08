package com.example.camerautil

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.io.InputStream

class CameraUtil {

    private lateinit var cameraProvider: ProcessCameraProvider

    private var imageCapture: ImageCapture? = null

    //starting camera function
    fun startCamera(
        activity: AppCompatActivity,    //activity
        cameraView: PreviewView,    //PreviewView For show camera capturing
        cameraFlip: Boolean     //boolean value true for back camera, false for front camera
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            //initializing camera provider and image capture
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { mPreview ->
                mPreview.setSurfaceProvider(cameraView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            //camera selector
            val cameraSelector =
                if (cameraFlip) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            //binding camera provider to the lifecycle
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    fun takePhoto(
        activity: AppCompatActivity,    //activity
        fileUtil: FileUtil,     //FileUtil to save file
        outputDirectory: File,      //Directory where user want to save images
        FILE_NAME_FORMAT: String    //image file name format
    ): ArrayList<String> {

        //media directory where captured image is saved for once
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, "Data").apply {
                mkdirs()
            }
        }
        val dir = if (mediaDir != null && mediaDir.exists()) mediaDir else activity.filesDir

        val imageCapture = imageCapture
        val photoFile = File(dir, "data.jpg")

        if (photoFile.exists()) {
            photoFile.delete()
        }

        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        //saving capture image in user defined directory
        imageCapture?.takePicture(outputOption,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    //fetching image from uri
                    val savedUri = Uri.fromFile(photoFile)
                    val ims: InputStream = activity.contentResolver.openInputStream(savedUri)!!
                    val bm = BitmapFactory.decodeStream(ims)

                    //saving file in user directory
                    fileUtil.saveImageToFolder(bm!!, outputDirectory, FILE_NAME_FORMAT)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })

        //returning all images from the directory
        return fileUtil.getImagesFromFile(outputDirectory)
    }
}