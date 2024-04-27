package com.example.jetcamera.presentation.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetcamera.R
import com.example.jetcamera.areCameraPermissionsAllowed
import com.example.jetcamera.presentation.viewmodel.CameraViewModel


@Composable
fun CameraScreen(
    activity: Activity,
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    val controller = remember {
        LifecycleCameraController(activity.applicationContext).apply {

            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
        }


    }
    val isRecording = cameraViewModel.state.collectAsState().value


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val lifeCycleOwner = LocalLifecycleOwner.current
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifeCycleOwner)
                }
            },
        )

        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {


            MiniButton(
                onClick = {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("content//media/internal/images/media")
                    ).also {
                        activity.startActivity(it)
                    }
                },
                icon = painterResource(id = R.drawable.ic_camera)
            )

            MiniButton(
                onClick = {

                    if (activity.areCameraPermissionsAllowed()) {
                        cameraViewModel.recordVideo(controller)
                    }
                },
                icon = painterResource(id = R.drawable.ic_camera)
            )
            MiniButton(
                onClick = {
                    if (activity.areCameraPermissionsAllowed()) {
                        cameraViewModel.onTakePhoto(controller)
                    }
                },
                icon = painterResource(id = R.drawable.ic_video)
            )
            MiniButton(
                onClick = {

                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA

                },
                icon = painterResource(id = R.drawable.ic_switch)
            )
        }
    }

}

@Composable

private fun MiniButton(
    onClick: () -> Unit,
    icon: Painter
) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .size(45.dp)
            .background(MaterialTheme.colorScheme.primary)
            .clickable {
                onClick()
            }
    ) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(26.dp))
    }
}