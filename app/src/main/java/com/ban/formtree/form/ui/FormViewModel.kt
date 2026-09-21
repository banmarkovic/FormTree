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

    private val isRefreshing = MutableStateFlow(true)
    private val selectedResponseIdsByQuestionId = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())

    private val pages: StateFlow<List<Page>> = formRepository.observeForm()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_SHARING_TIMEOUT_MILLIS),
            initialValue = emptyList(),
        )

    val uiState: StateFlow<FormUiState> = combine(
        pages,
        isRefreshing,
        selectedResponseIdsByQuestionId,
    ) { currentPages, refreshing, selectedResponseIds ->
        FormUiState(
            isLoading = refreshing && currentPages.isEmpty(),
            items = currentPages.toListItems(selectedResponseIds),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_SHARING_TIMEOUT_MILLIS),
        initialValue = FormUiState(),
    )

    init {
        refresh()
    }

    fun onResponseClick(questionId: Long, responseId: Long) {
        val question = findChoiceQuestion(
            items = pages.value.flatMap { it.items },
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
