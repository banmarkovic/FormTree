package com.ban.formtree.imagedetails.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.ban.formtree.core.navigation.ImageDetailsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val uiState: ImageDetailsUiState = savedStateHandle.toRoute<ImageDetailsRoute>().let { route ->
        ImageDetailsUiState(src = route.src, title = route.title)
    }
}
