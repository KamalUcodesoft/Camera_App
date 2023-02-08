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
        //required permission for app
        val REQUIRED_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        //permission code
        const val PERMISSION_REQ_CODE = 101
        //image file name format
        const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SS"

        //list for gallery images
        val galleryImagesList = MutableLiveData<ArrayList<String>>()
        //list for clicked images by user
        val clickedImagesList = MutableLiveData<ArrayList<String>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //handling back press for finishing activity
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        when {
            allPermissionGranted() -> {     //adding fragment to container when all permission is granted
                supportFragmentManager.beginTransaction().add(R.id.fragmentConV, CameraFragment())
                    .addToBackStack(resources.getString(R.string.camera_fragment)).commit()
            }
            shouldShowRequestPermissionRationale(REQUIRED_PERMISSION[0]) -> {
                //showing why this permission is required
                requestPermissionRationaleDialog(REQUIRED_PERMISSION[0] + resources.getString(R.string.required_permission_msg))
            }
            shouldShowRequestPermissionRationale(REQUIRED_PERMISSION[1]) -> {
                //showing why this permission is required
                requestPermissionRationaleDialog(REQUIRED_PERMISSION[1] + resources.getString(R.string.required_permission_msg))
            }
            else -> {
                //asking user for permissions
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQ_CODE)
            }
        }
    }

    //required permission reason dialog
    private fun requestPermissionRationaleDialog(msg: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(resources.getString(R.string.permission_required))
            .setMessage(msg.subSequence(19, msg.length)).setCancelable(false)
            .setPositiveButton(resources.getString(R.string.go_to_settings)) { _, _ ->
                //intent for directing user to app settings for allowing permissions
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts(
                            resources.getString(R.string.pack), packageName, null
                        )
                    )
                )
            }.setNegativeButton(resources.getString(R.string.no)) { _, _ ->
                //finishing activity
                finish()
            }.show()
    }

    //check all permission is granted or not
    private fun allPermissionGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    //handling permission result
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