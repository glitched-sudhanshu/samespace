package com.example.samespace.ui.forYou

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForYouViewModel : ViewModel() {
    private val _text =
        MutableLiveData<String>().apply {
            value = "This is home Fragment"
        }
    val text: LiveData<String> = _text
}
