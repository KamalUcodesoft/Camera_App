package com.example.camerautil

class CameraLib {
    private val cameraUtil = CameraUtil()
    private val fileUtil = FileUtil()

    fun getCameraUtil(): CameraUtil = cameraUtil

    fun getFileUtil(): FileUtil = fileUtil
}