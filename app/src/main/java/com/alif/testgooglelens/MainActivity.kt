package com.alif.testgooglelens

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        const val MY_PERMISSIONS_MULTIPLE = 1
        const val REQUEST_IMAGE = 101
    }

    var listGalery: ArrayList<Image> = ArrayList()
    lateinit var image: InputImage

    lateinit var loading: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        initLoading()

        initListerner()
    }

    private fun initListerner() {
        btSelectImage.setOnClickListener {
            selectImageMethod(REQUEST_IMAGE)
        }
    }

    fun selectImageMethod(idRequest: Int) {
        val dialog = Dialog(this, R.style.DialogTheme)
        dialog.setContentView(R.layout.dialog_image_method)
        dialog.window?.setBackgroundDrawableResource(R.color.colorTransparent)

        val camera = dialog.findViewById(R.id.llCamera) as LinearLayout
        val gallery = dialog.findViewById(R.id.llGallery) as LinearLayout
        camera.setOnClickListener {
            val maxSize = 1
            ImagePicker.with(this)
                .setToolbarColor("#316dad")
                .setBackgroundColor("#FFFFFF")
                .setCameraOnly(true)
                .setMaxSize(maxSize)
                .setRequestCode(idRequest)
                .start()
            dialog.dismiss()
        }
        gallery.setOnClickListener {
            val maxSize = 1
            ImagePicker.with(this)
                .setToolbarColor("#316dad")
                .setBackgroundColor("#FFFFFF")
                .setMaxSize(maxSize)
                .setShowCamera(false)
                .setFolderMode(false)
                .setRequestCode(idRequest)
                .start()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun scanImage() {
        val uri = Uri.fromFile(File(listGalery[0].path))
        try {
            image = InputImage.fromFilePath(this, uri)
            var recognizer = TextRecognition.getClient()

            val result = recognizer.process(image)
                .addOnSuccessListener {
                    tvOutput.setText(it.text)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "error : "+ it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }catch (e: Exception){
            Toast.makeText(this, "error : "+ e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ( resultCode == Activity.RESULT_OK && android.R.attr.data != null) {
            if (requestCode == REQUEST_IMAGE){
                listGalery.clear()
                listGalery.addAll(data?.getParcelableArrayListExtra(Config.EXTRA_IMAGES)!!)
                if (listGalery.size > 0) {
                    Glide.with(this)
                        .load(listGalery[0].path)
                        .into(ivImagePreview)

                    scanImage()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initLoading() {
        loading = Dialog(this, R.style.DialogTheme)
        loading.setContentView(R.layout.dialog_loading)
        loading.window?.setBackgroundDrawableResource(R.color.colorTransparent)
        loading.setCancelable(false)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                MY_PERMISSIONS_MULTIPLE
            )
        }
    }
}