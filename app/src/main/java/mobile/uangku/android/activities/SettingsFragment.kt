package mobile.uangku.android.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.fragment_settings.*
import com.google.android.material.snackbar.Snackbar
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*




class SettingsFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context
    val PHOTO_PROFILE = 123
    var imageType: Int = 0
    var bitmap: Bitmap? = null
    var currentPhotoUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: add import image from gallery
        profilePhoto.setOnClickListener {
            openCaptureOption(PHOTO_PROFILE)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context!!
    }

    override fun onResume() {
        super.onResume()
    }

    fun openCaptureOption(status_code: Int) {
        imageType = status_code
        if (ContextCompat.checkSelfPermission(fragmentContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 50)
            } else {
                Alert.dialog(fragmentContext, "Anda harus memperbolehkan akses kamera sebelum menggunakan fitur ini.")
            }
        } else {
            dispatchTakePictureIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PHOTO_PROFILE && currentPhotoUri != null) {
            bitmap = MediaStore.Images.Media.getBitmap(fragmentContext.contentResolver, currentPhotoUri)
            if (bitmap != null) {
                submitPhoto()
            }
        }

    }

    fun submitPhoto() {
        var profileFile = ImageUtils.bitmapToFile(fragmentContext, "temp_profile_photo.jpeg", bitmap!!)

        AndroidNetworking.upload(API.getBaseURL(fragmentContext) + "auth/change-profile-photo")
            .addMultipartFile("photo", profileFile)
            .addMultipartParameter("token", API.TOKEN)
            .addMultipartParameter("session_key", Session.key(fragmentContext))
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Session.saveUserData(fragmentContext, response)
                    activity?.let { Snackbar.make(it.findViewById(android.R.id.content), "Edit Photo berhasil dilakukan", Snackbar.LENGTH_SHORT).show() }
                }

                override fun onError(error: ANError) {
                    API.handleErrorResponse(fragmentContext, error)
                }
            })
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(fragmentContext.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Toast.makeText(fragmentContext, "Failed to save photo", Toast.LENGTH_SHORT).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    currentPhotoUri = FileProvider.getUriForFile(
                        fragmentContext,
                        "mobile.uangku.android.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                    startActivityForResult(takePictureIntent, imageType)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = fragmentContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "IMAGE_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

}