package com.android.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.android.happyplaces.R
import com.android.happyplaces.database.DatabaseHandler
import com.android.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class AddHappyPlaceActivity : AppCompatActivity(), OnClickListener {

    private val cal: Calendar = Calendar.getInstance()
    private lateinit var btnSave: Button
    private var editId: Int? = null
    private lateinit var ivPlaceImage: AppCompatImageView
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var imageBitmap: Bitmap? = null
    private lateinit var savedImageUri: Uri
    private lateinit var etTitle: AppCompatEditText
    private lateinit var etDescription: AppCompatEditText
    private lateinit var etDate: AppCompatEditText
    private lateinit var etLocation: AppCompatEditText
    private lateinit var tvAddImage: TextView
    private var mLongitude = 0.0
    private var mLatitude = 0.0

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val DIRECTORY = "HappyPlacesImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        val toolBarAddPlace = findViewById<Toolbar>(R.id.toolbarAddPlace)
        setSupportActionBar(toolBarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        ivPlaceImage = findViewById(R.id.iv_place_image)
        btnSave = findViewById(R.id.btn_save)
        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etDate = findViewById(R.id.et_date)
        etLocation = findViewById(R.id.et_location)
        tvAddImage = findViewById(R.id.tv_add_image)

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            setDate()
        }

        setDate()
        etDate.setOnClickListener(this)
        tvAddImage.setOnClickListener(this)
        btnSave.setOnClickListener(this)

        val parcelable: HappyPlaceModel? =
            intent.getParcelableExtra(MainActivity.HAPPY_PLACE_DETAIL)
        parcelable?.let {
            etTitle.setText(it.title)
            etDescription.setText(it.description)
            etDate.setText(it.date)
            etLocation.setText(it.location)
            ivPlaceImage.setImageURI(Uri.parse(it.image))
            btnSave.text = "UPDATE"
            editId = it.id

            savedImageUri = Uri.parse(it.image)
        }
    }

    private fun setDate() {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy")
        etDate.setText(dateFormatter.format(cal.time).toString())
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                showDatePickerDialog()
            }
            R.id.tv_add_image -> {
                showAddImageDialog()
            }
            R.id.btn_save -> {
                imageBitmap?.let {
                    savedImageUri = saveImageToInternalStorage(imageBitmap)
                    Log.i("SAVED_IMAGE", "uri: $savedImageUri")
                }
                if (isValidInput()) {
                    val addHappyPlace = saveHappyPlace()
                    if (addHappyPlace > 0) {
                        setResult(Activity.RESULT_OK);
                        finish()
                    }
                    Log.i("Happy Places List", "${getHappyPlaces()}")
                }

            }
        }
    }

    private fun isValidInput(): Boolean {
        when {
            etTitle.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
            }
            etDescription.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                    .show()
            }
            etLocation.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                    .show()
            }
            savedImageUri == null -> {
                Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
            }
            else -> return true
        }
        return false
    }

    private fun saveHappyPlace(): Long {
        val happyPlaceModel = HappyPlaceModel(
            editId ?: 0,
            etTitle.text.toString(),
            savedImageUri.toString(),
            etDescription.text.toString(),
            etDate.text.toString(),
            etLocation.text.toString(),
            mLatitude,
            mLongitude
        )

        val dbHandler = DatabaseHandler(this)

        return editId?.let {
            Log.i("HAPPY_PLACE_MODEL_UPDATE", "PAYLOAD: $happyPlaceModel")
            dbHandler.updateHappyPlace(happyPlaceModel).toLong()
        } ?: let {
            Log.i("HAPPY_PLACE_MODEL_ADD", "PAYLOAD: $happyPlaceModel")
            dbHandler.addHappyPlace(happyPlaceModel)
        }
    }

    private fun getHappyPlaces(): ArrayList<HappyPlaceModel> {
        val dbHandler = DatabaseHandler(this)

        return dbHandler.getHappyPlacesList()
    }

    private fun showAddImageDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        val dialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
        pictureDialog.setItems(dialogItems) { _, which ->
            when (which) {
                0 -> selectPhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.create()
        pictureDialog.show()
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            this@AddHappyPlaceActivity,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                showRationalDialogForPermission()
            }

        }).onSameThread().check()
    }

    private fun selectPhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                showRationalDialogForPermission()
            }

        }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY -> {
                    imageBitmap = uriToBitmap(data!!.data!!)!!
                    ivPlaceImage.setImageBitmap(imageBitmap)
                }
                CAMERA -> {
                    imageBitmap = data!!.extras!!.get("data") as Bitmap
                    ivPlaceImage.setImageBitmap(imageBitmap)
                }
            }
        }
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap?): Uri {

        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir(DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("Looks like you have turned off permission for this feature. You can to go SETTINGS to enable permissions")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, yes ->
                dialog.dismiss()
            }
    }
}