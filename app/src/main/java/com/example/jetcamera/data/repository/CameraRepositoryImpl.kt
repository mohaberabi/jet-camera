package com.example.jetcamera.data.repository

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.Matrix
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import com.example.jetcamera.R
import com.example.jetcamera.domain.repository.CameraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(
    private val app: Application
) : CameraRepository {

    private var recording: Recording? = null
    override suspend fun takePhoto(
        controller:
        LifecycleCameraController
    ) {

        controller.takePicture(
            ContextCompat.getMainExecutor(app),
            object : ImageCapture.OnImageCapturedCallback() {
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val matrix = android.graphics.Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())

                    }

                    val imageBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        savePhoto(imageBitmap)
                    }
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun recordVideo(
        controller:
        LifecycleCameraController
    ) {

        if (recording != null) {
            recording?.stop()
            recording = null
            return
        }
        val timeMillis = System.currentTimeMillis()
        val file = File(
            app.filesDir,
            "${timeMillis}_video.mp4"
        )
        recording = controller.startRecording(
            FileOutputOptions.Builder(file).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(app)
        ) { event ->
            if (event is VideoRecordEvent.Finalize) {
                if (event.hasError()) {
                    recording?.close()
                    recording = null
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveVideo(file)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun savePhoto(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {

            val resolver: ContentResolver = app.contentResolver
            val imageCollection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val appName = app.getString(R.string.app_name)
            val timeMillis = System.currentTimeMillis()

            // store meta data in the images database
            val imageContentValues = ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "${timeMillis}_image.jpg"
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/${appName}"
                )
                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpg"
                )
                put(
                    MediaStore.Images.Media.DATE_TAKEN,
                    timeMillis,
                )
                put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
                )
            }

            val imageUri: Uri? = resolver.insert(
                imageCollection, imageContentValues
            )
            imageUri?.let { uri ->
                try {
                    resolver.openOutputStream(uri)?.let { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }

                    imageContentValues.clear()
                    imageContentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, imageContentValues, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                    resolver.delete(uri, null, null)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveVideo(file: File) {


        withContext(Dispatchers.IO) {
            val resolver = app.contentResolver
            val videoCollection = MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val appname = app.getString(R.string.app_name)
            val millis = System.currentTimeMillis()
            val videoVals = ContentValues().apply {

                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)

                put(MediaStore.MediaColumns.RELATIVE_PATH, millis)
                put(MediaStore.MediaColumns.DATE_ADDED, millis)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.DATE_MODIFIED, millis)
                put(MediaStore.MediaColumns.DATE_TAKEN, millis)
                put(MediaStore.MediaColumns.IS_PENDING, 1)

            }

            val videoUri: Uri? = resolver.insert(
                videoCollection, videoVals
            )
            videoUri?.let { uri ->
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        resolver.openInputStream(Uri.fromFile(file))?.use { input ->
                            input.copyTo(outputStream)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    resolver.delete(uri, null, null)
                }
            }
        }
    }
}