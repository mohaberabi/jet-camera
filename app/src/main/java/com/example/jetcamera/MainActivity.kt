package com.example.jetcamera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jetcamera.presentation.screens.CameraScreen
import com.example.jetcamera.ui.theme.JetCameraTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetCameraTheme {
                if (!areCameraPermissionsAllowed()) {
                    ActivityCompat.requestPermissions(
                        this, CAMERA_PERMISSIONS, 100
                    )
                }

                CameraScreen(activity = this)
            }
        }
    }


    companion object {
        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}


fun Activity.areCameraPermissionsAllowed(): Boolean {
    return MainActivity.CAMERA_PERMISSIONS.all { permisison ->
        ContextCompat.checkSelfPermission(
            applicationContext,
            permisison
        ) == PackageManager.PERMISSION_GRANTED
    }
}

