package com.example.imagepicker2


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files.createFile
import java.text.SimpleDateFormat
import java.util.*


const val PERMISSION_REQUEST_CAMERA = 0
const val CAMERA_PHOTO_REQUEST = 1
const val GALLERY_PHOTO_REQUEST = 2


class MainActivity : AppCompatActivity() {

    lateinit var employee_photo:ImageView
    lateinit var photo_from_camera:Button
    lateinit var photo_from_gallery:Button
    private var selectedPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photo_from_camera=findViewById(R.id.button)
        photo_from_gallery=findViewById(R.id.button2)
        employee_photo=findViewById(R.id.imageView)

        photo_from_camera.setOnClickListener{
            clickPhotoAfterPermission(it)
        }

        photo_from_gallery.setOnClickListener{
            pickPhoto()
        }

    }
//


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//    }


    private fun clickPhotoAfterPermission(view: View){
        if (ActivityCompat.checkSelfPermission(this!!, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            clickPhoto()
        } else {
            requestCameraPermission(view)
        }
    }


    private fun requestCameraPermission(view: View) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity!!, Manifest.permission.CAMERA)) {
            val snack = Snackbar.make(view, "We need your permission to take a photo. " +
                    "When asked please give the permission", Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                requestPermissions(arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )
            })
            snack.show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                clickPhoto()
            } else {
                Toast.makeText(this@MainActivity, "Permission denied to use camera",
                    Toast.LENGTH_SHORT). show()
            }
        }
    }
    fun createFile(context: Context, folder: String, ext: String): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filesDir: File? = context.getExternalFilesDir(folder)
        val newFile = File(filesDir, "$timeStamp.$ext")
        newFile.createNewFile()
        return newFile
    }


    private fun clickPhoto(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(this@MainActivity!!.packageManager)?.also {
                val photoFile: File? = try {
                    createFile(
                        this@MainActivity!!,
                        Environment.DIRECTORY_PICTURES,
                        "jpg"
                    )
                } catch (ex: IOException) {

                    Toast.makeText(this@MainActivity!!, getString(R.string.create_file_error, ex.message),
                        Toast.LENGTH_SHORT).show()

                    null
                }
                photoFile?.also {
                    selectedPhotoPath = it.absolutePath
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this@MainActivity!!,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent,
                        CAMERA_PHOTO_REQUEST
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                CAMERA_PHOTO_REQUEST -> {
                    val file = File(selectedPhotoPath)
                    val uri = Uri.fromFile(file)
                    employee_photo.setImageURI(uri)
                    employee_photo.tag = uri.toString()
                }
                GALLERY_PHOTO_REQUEST ->{
                    val photoFile: File? = try {
                        createFile(
                            this@MainActivity!!,
                            Environment.DIRECTORY_PICTURES,
                            "jpg"
                        )
                    } catch (ex: IOException) {
                        Toast.makeText(this@MainActivity!!, getString(R.string.create_file_error, ex.message),
                            Toast.LENGTH_SHORT).show()
                        null
                    }
                    photoFile?.also {
                        selectedPhotoPath = it.absolutePath
                        val resolver = this@MainActivity!!.applicationContext.contentResolver
                        resolver.openInputStream(data!!.data!!).use { stream ->
                            val output = FileOutputStream(photoFile)
                            stream!!.copyTo(output)
                        }
                        val uri = Uri.fromFile(photoFile)
                        employee_photo.setImageURI(uri)
                        employee_photo.tag = uri.toString()
                    }
                }
            }
        }
    }

    private fun pickPhoto(){
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent,
            GALLERY_PHOTO_REQUEST
        )

    }
}