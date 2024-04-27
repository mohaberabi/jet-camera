package com.example.jetcamera.domain.repository

import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.Lifecycle

interface CameraRepository {

    suspend fun takePhoto(controller : LifecycleCameraController)
    suspend fun recordVideo(controller : LifecycleCameraController)

}