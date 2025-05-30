package com.live.pastransport.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.live.pastransport.BuildConfig
import com.live.pastransport.R
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


abstract class CameraActivity : AppCompatActivity() {


    private var type = 0 // 0 for image camera and 1 for pdf
    private val REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 102
    private val PDF_PICKER_REQUEST_CODE = 1001 // Arbitrary request code for PDF picker
    private lateinit var mImageFile: File
    private var mActivity: Activity? = null
    private var mCode = 0

    @RequiresApi(Build.VERSION_CODES.M)
    open fun getImage(activity: Activity, code: Int, type: Int) {
        this.type = type
        mActivity = activity
        mCode = code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handlePermissionRequest(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
                )
            )
        } else {
            handlePermissionRequest(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    private fun handlePermissionRequest(permissions: Array<String>) {
        if (!hasPermissions(permissions)) {
            requestPermissions(permissions)
        } else {
            imageDialog()
        }
    }

    private fun imageDialog() {
        // Initialize the list with default options
        val options = mutableListOf("Camera", "Gallery")

        // Add "PDF" option if type is 1
        if (type == 1) {
            options.add("PDF")
        }

        // Create and show the AlertDialog
        val dialog = android.app.AlertDialog.Builder(mActivity!!).apply {
            setTitle("Choose Image Source")
            setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Camera" -> captureImage(mActivity!!)
                    "Gallery" -> openGallery(mActivity!!)
                    "PDF" -> openPdfSelector(mActivity!!)
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
        dialog.show()
    }

    private fun openPdfSelector(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        // Launch the intent with a request code
        activity.startActivityForResult(
            Intent.createChooser(intent, "Select PDF"),
            PDF_PICKER_REQUEST_CODE
        )
    }

    open fun captureImage(activity: Activity) {
        mActivity = activity
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        try {
            createImageFile(activity, imageFileName, ".jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(
            activity.applicationContext,
            BuildConfig.APPLICATION_ID + ".provider",
            mImageFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, name: String, extension: String) {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mImageFile = File.createTempFile(
            name,
            extension,
            storageDir
        )
    }

    open fun openGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                mActivity!!,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    open fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            mActivity!!,
            permissions,
            REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage(mActivity!!, mCode, type)
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                checkPermissionDenied(permissions)
            }
        }
    }

    private fun checkPermissionDenied(permissions: Array<out String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                val mBuilder = android.app.AlertDialog.Builder(mActivity!!)
                val dialog: android.app.AlertDialog =
                    mBuilder.setTitle(getString(R.string.alert_title))
                        .setMessage(getString(R.string.permission_required))

                        .setPositiveButton(
                            getString(R.string.ok)
                        ) { dialog, which -> requestPermissions(permissions as Array<String>) }
                        .setNegativeButton(
                            getString(R.string.cancel)
                        ) { dialog, which ->

                        }.create()
                dialog.setOnShowListener {
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(
                            mActivity!!, R.color.black20
                        )
                    )
                }
                dialog.show()
            }
        }
    }

    var pdfFile: File? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PDF_PICKER_REQUEST_CODE -> {
                    // Handle PDF selection
                    data?.data?.let { uri ->

                        val sUri: Uri? = data.data
                        val fileType = getFileType(sUri!!)
                        pdfFile = File(
                            externalCacheDir,
                            Calendar.getInstance().time.time.toString() + "." + fileType
                        )

                        contentResolver.openInputStream(sUri)
                            ?.let { copyInputStreamToFile(inputStream = it, file = pdfFile!!) }


                        selectedImage(pdfFile.toString(), mCode,sUri)

                    } ?: run {
                        Toast.makeText(this, "No PDF selected", Toast.LENGTH_SHORT).show()
                    }
                }

                CAMERA_REQUEST_CODE -> {
                    // Handle image capture from camera
                    val uri = Uri.fromFile(mImageFile)
                    val uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )
                    uCrop.start(this)
                }

                GALLERY_REQUEST_CODE -> {
                    // Handle image selection from gallery
                    data?.data?.let { galleryUri ->
                        val uCrop = UCrop.of(
                            galleryUri,
                            Uri.fromFile(
                                File(
                                    cacheDir,
                                    UUID.randomUUID().toString() + "CropImage.png"
                                )
                            )
                        )
                        uCrop.start(this)
                    } ?: run {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                UCrop.REQUEST_CROP -> {
                    // Handle image cropping result
                    val emptyUri = Uri.EMPTY
                    val resultUri = UCrop.getOutput(data!!)
                    if (resultUri != null) {
                        selectedImage(resultUri.path.toString(), mCode, emptyUri)
                    } else {
                        Toast.makeText(this, "Error during cropping", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    // Unrecognized request code
                    Toast.makeText(this, "Unhandled request code: $requestCode", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            // Handle UCrop error
            val cropError = data?.let { UCrop.getError(it) }
            cropError?.printStackTrace()
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun selectedImage(imagePath: String?, code: Int?, sUri: Uri)
    private fun getFileType(uri: Uri): String? {
        val r = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(uri))
    }

    @Throws(IOException::class)
    fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        try {
            FileOutputStream(file, false).use { outputStream ->
                var read: Int
                val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                while (inputStream.read(bytes).also { read = it } != -1) {
                    outputStream.write(bytes, 0, read)
                }
            }
        } catch (e: IOException) {
            Log.e("Failed to load file: ", e.message.toString())
        }
    }



}