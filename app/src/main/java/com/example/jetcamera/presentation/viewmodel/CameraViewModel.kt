package com.example.jetcamera.presentation.viewmodel

import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetcamera.domain.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
) : ViewModel() {


    private val _state = MutableStateFlow(false)

    val state: StateFlow<Boolean>
        get() = _state


    fun recordVideo(controller: LifecycleCameraController) {

        _state.update {
            !it
        }
        viewModelScope.launch {
            cameraRepository.recordVideo(controller)
        }

    }

    fun onTakePhoto(controller: LifecycleCameraController) {

        viewModelScope.launch {
            cameraRepository.takePhoto(controller)
        }

    }

}