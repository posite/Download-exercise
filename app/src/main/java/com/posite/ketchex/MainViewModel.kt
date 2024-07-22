package com.posite.ketchex

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    private val _size = mutableStateOf(0L)
    val size get() = _size

    private val _progress = mutableStateOf(0f)
    val progress get() = _progress

    private val _speed = mutableStateOf(0f)
    val speed get() = _speed

    fun setSize(size: Long) {
        _size.value = size
    }

    fun setProgress(progress: Float) {
        _progress.value = progress
    }

    fun setSpeed(speed: Float) {
        _speed.value = speed
    }
}