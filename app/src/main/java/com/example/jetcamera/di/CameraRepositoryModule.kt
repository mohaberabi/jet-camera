package com.example.jetcamera.di

import com.example.jetcamera.data.repository.CameraRepositoryImpl
import com.example.jetcamera.domain.repository.CameraRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class CameraRepositoryModule {


    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraRepositoryImpl:
        CameraRepositoryImpl
    ): CameraRepository
}