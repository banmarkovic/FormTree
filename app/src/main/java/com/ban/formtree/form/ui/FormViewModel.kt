package com.ban.formtree.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.repository.FormRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FormViewModel @Inject constructor(
    private val formRepository: FormRepository,
) : ViewModel() {

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Refreshing)
    private val selectedResponseIdsByQuestionId = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())

    private val pages: StateFlow<List<Page>?> = formRepository.observeForm()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_SHARING_TIMEOUT_MILLIS),
            initialValue = null,
        )

    val uiState: StateFlow<FormUiState> = combine(
        pages,
        refreshStatus,
        selectedResponseIdsByQuestionId,
    ) { currentPages, refreshStatus, selectedResponseIds ->
        when {
            currentPages == null -> FormUiState.Loading
            currentPages.isEmpty() && refreshStatus is RefreshStatus.Refreshing -> FormUiState.Loading
            currentPages.isEmpty() && refreshStatus is RefreshStatus.Failed ->
                FormUiState.Error(failedAttempts = refreshStatus.attempts)

            else -> FormUiState.Data(
                items = currentPages.toListItems(selectedResponseIds),
                showRefreshFailedNotice = refreshStatus is RefreshStatus.Failed,
                showRefreshIndicator = refreshStatus is RefreshStatus.Refreshing,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_SHARING_TIMEOUT_MILLIS),
        initialValue = FormUiState.Loading,
    )

    init {
        refresh()
    }

    fun onRetryClick() {
        refresh()
    }

    fun onRefreshFailedNoticeDismiss() {
        refreshStatus.value = RefreshStatus.Idle
    }

    fun onResponseClick(questionId: Long, responseId: Long) {
        val question = findChoiceQuestion(
            items = pages.value.orEmpty().flatMap { it.items },
            questionId = questionId,
        ) ?: return
        selectedResponseIdsByQuestionId.update { selections ->
            val selectedResponseIds = selections[questionId].orEmpty()
            val updatedResponseIds = when {
                question.responseSet.multipleSelection ->
                    if (responseId in selectedResponseIds) {
                        selectedResponseIds - responseId
                    } else {
                        selectedResponseIds + responseId
                    }

                responseId in selectedResponseIds -> emptySet()
                else -> setOf(responseId)
            }
            if (updatedResponseIds.isEmpty()) {
                selections - questionId
            } else {
                selections + (questionId to updatedResponseIds)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val previousFailures = (refreshStatus.value as? RefreshStatus.Failed)?.attempts ?: 0
            refreshStatus.value = RefreshStatus.Refreshing
            try {
                formRepository.refreshForm()
                refreshStatus.value = RefreshStatus.Idle
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                refreshStatus.value = RefreshStatus.Failed(attempts = previousFailures + 1)
            }
        }
    }

    private sealed interface RefreshStatus {
        data object Refreshing : RefreshStatus
        data object Idle : RefreshStatus
        data class Failed(val attempts: Int) : RefreshStatus
    }

    private companion object {
        const val STOP_SHARING_TIMEOUT_MILLIS = 5_000L
    }
}

private fun findChoiceQuestion(items: List<FormItem>, questionId: Long): FormItem.ChoiceQuestion? {
    items.forEach { item ->
        when (item) {
            is FormItem.ChoiceQuestion -> if (item.id == questionId) return item
            is FormItem.Section -> findChoiceQuestion(item.items, questionId)?.let { return it }
            is FormItem.TextQuestion, is FormItem.ImageQuestion -> Unit
        }
    }
    return null
}
