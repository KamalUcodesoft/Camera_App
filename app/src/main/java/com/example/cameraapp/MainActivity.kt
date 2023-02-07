package com.example.cameraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

class MainActivity : AppCompatActivity() {

    companion object {
        val REQUIRED_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        const val PERMISSION_REQ_CODE = 101
        const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SS"

        val galleryImagesList = MutableLiveData<ArrayList<String>>()
        val clickedImagesList = MutableLiveData<ArrayList<String>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                supportFragmentManager.popBackStack()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        when {
            allPermissionGranted() -> {
                supportFragmentManager.beginTransaction().add(R.id.fragmentConV, CameraFragment())
                    .addToBackStack("CameraFragment").commit()
            }
            shouldShowRequestPermissionRationale(REQUIRED_PERMISSION[0]) -> {
                requestPermissionRationaleDialog("${REQUIRED_PERMISSION[0]} permission is required please go to the settings and allow permissions")
            }
            shouldShowRequestPermissionRationale(REQUIRED_PERMISSION[1]) -> {
                requestPermissionRationaleDialog("${REQUIRED_PERMISSION[1]} permission is required please go to the settings and allow permissions")
            }
            else -> {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQ_CODE)
            }
        }
    }

    private fun requestPermissionRationaleDialog(msg: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Permission required").setMessage(msg.subSequence(19, msg.length))
            .setCancelable(false).setPositiveButton("Go to settings") { _, _ ->
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts(
                            "package", packageName, null
                        )
                    )
                )
            }.setNegativeButton("no") { _, _ ->
                finish()
            }.show()
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (requestCode != PERMISSION_REQ_CODE) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQ_CODE)
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}