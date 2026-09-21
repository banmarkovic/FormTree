package com.ban.formtree.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ban.formtree.form.domain.repository.FormRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FormViewModel @Inject constructor(
    private val formRepository: FormRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(true)

    val uiState: StateFlow<FormUiState> = combine(
        formRepository.observeForm(),
        isRefreshing,
    ) { pages, refreshing ->
        FormUiState(
            isLoading = refreshing && pages.isEmpty(),
            items = pages.toListItems(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_SHARING_TIMEOUT_MILLIS),
        initialValue = FormUiState(),
    )

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                formRepository.refreshForm()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // TODO handle error state
            }
            isRefreshing.value = false
        }
    }

    private companion object {
        const val STOP_SHARING_TIMEOUT_MILLIS = 5_000L
    }
}
