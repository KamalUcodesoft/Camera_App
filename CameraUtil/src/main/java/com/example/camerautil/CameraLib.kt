package com.example.camerautil

class CameraLib {

    //CameraUtil Instance
    private val cameraUtil = CameraUtil()
    //FileUtil Instance
    private val fileUtil = FileUtil()

    fun getCameraUtil(): CameraUtil = cameraUtil    //return CameraUtil Instance

    fun getFileUtil(): FileUtil = fileUtil  //return FileUtil Instance
}