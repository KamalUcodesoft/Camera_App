package com.example.cameraapp

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cameraapp.MainActivity.Companion.FILE_NAME_FORMAT
import com.example.cameraapp.MainActivity.Companion.clickedImagesList
import com.example.camerautil.FileUtil
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class GalleryAdapter(
    private val list: ArrayList<String>,
    private val context: Context,
    private val fileUtil: FileUtil,
    private val outputDir: File,
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView

        init {
            imageView = view.findViewById(R.id.galleryIv)

            view.setOnClickListener {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.dialog_img)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )

                val img = dialog.findViewById<ImageView>(R.id.image2)
                val saveBtn = dialog.findViewById<ExtendedFloatingActionButton>(R.id.dialogSaveBtn)
                val crossBtn = dialog.findViewById<ImageView>(R.id.dialogCrossBtn)

                Glide.with(context).load(list[adapterPosition]).into(img)

                saveBtn.setOnClickListener {
                    saveBtn.visibility = View.GONE
                    val bm = BitmapFactory.decodeFile(list[adapterPosition])
                    fileUtil.saveImageToFolder(
                        bm, outputDir, FILE_NAME_FORMAT
                    )

                    MainScope().launch {
                        delay(1000)
                        clickedImagesList.postValue(fileUtil.getImagesFromFile(outputDir))
                    }

                    Toast.makeText(context, "image selected", Toast.LENGTH_SHORT).show()
                }

                crossBtn.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        fun bind(image: String) {
            Glide.with(context).load(image).into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}