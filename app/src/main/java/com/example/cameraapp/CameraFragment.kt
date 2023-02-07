package com.example.cameraapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraapp.MainActivity.Companion.FILE_NAME_FORMAT
import com.example.cameraapp.MainActivity.Companion.clickedImagesList
import com.example.cameraapp.MainActivity.Companion.galleryImagesList
import com.example.cameraapp.databinding.FragmentCameraBinding
import com.example.cameraapp.databinding.SelectedBottomSheetBinding
import com.example.camerautil.CameraLib
import com.example.camerautil.CameraUtil
import com.example.camerautil.FileUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var selectedImagesBottomSheetBinding: SelectedBottomSheetBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraUtil: CameraUtil
    private lateinit var fileUtil: FileUtil
    private lateinit var outputDir: File
    private lateinit var bottomSheetDialog: Dialog

    private var flip = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        initialise()
        observers()
        bottomSheetDialogInit()

        binding.galleryImagesRecV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraUtil.startCamera(requireActivity() as AppCompatActivity, binding.cameraPreview, flip)

        binding.galleryBtn.setOnClickListener {
            bottomSheetDialog.show()
        }

        binding.flipBtn.setOnClickListener {
            flip = !flip
            cameraUtil.startCamera(
                requireActivity() as AppCompatActivity, binding.cameraPreview, flip
            )
        }

        binding.takePhotoBtn.setOnClickListener {

            val takePhotoAnim =
                AnimationUtils.loadAnimation(requireContext(), R.anim.photo_click_anim)
            takePhotoAnim.duration = 500
            binding.takePhotoBtn.startAnimation(takePhotoAnim)
            cameraUtil.takePhoto(
                requireActivity() as AppCompatActivity, fileUtil, outputDir, FILE_NAME_FORMAT
            )

            getImagesList()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    private fun initialise() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraLib = CameraLib()
        cameraUtil = cameraLib.getCameraUtil()
        fileUtil = cameraLib.getFileUtil()

        val path = Environment.getExternalStorageDirectory().toString() + "/DCIM/CameraApp"
        outputDir = fileUtil.getDir(path)

        getImagesList()
    }

    private fun getImagesList() {
        MainScope().launch {
            delay(1000)
            galleryImagesList.postValue(fileUtil.fetchImagesFromGallery(requireContext()))
            clickedImagesList.postValue(fileUtil.getImagesFromFile(outputDir))
        }
    }

    private fun observers() {
        galleryImagesList.observe(viewLifecycleOwner) {
            binding.galleryImagesRecV.adapter =
                GalleryAdapter(it, requireContext(), fileUtil, outputDir)
        }
        clickedImagesList.observe(viewLifecycleOwner) {
            selectedImagesBottomSheetBinding.dialogClickedImagesRecV.adapter =
                ClickedAdapter(it, requireContext())
        }
    }

    private fun bottomSheetDialogInit() {
        selectedImagesBottomSheetBinding = SelectedBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog = Dialog(requireContext())
        bottomSheetDialog.setContentView(selectedImagesBottomSheetBinding.root)

        selectedImagesBottomSheetBinding.dialogClickedImagesRecV.layoutManager =
            GridLayoutManager(requireContext(), 4)

        bottomSheetDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500)
        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheetDialog.window?.setGravity(Gravity.BOTTOM)
    }
}