package com.ban.formtree.form.ui

import app.cash.turbine.test
import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.model.Response
import com.ban.formtree.form.domain.model.ResponseSet
import com.ban.formtree.form.domain.repository.FormRepository
import com.ban.formtree.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val formRepository = mockk<FormRepository>()

    @Test
    fun `refreshes the form when started`() = runTest {
        every { formRepository.observeForm() } returns flowOf(emptyList())
        coJustRun { formRepository.refreshForm() }

        FormViewModel(formRepository)

        coVerify(exactly = 1) { formRepository.refreshForm() }
    }

    @Test
    fun `flattens the form tree into list items with hierarchy depths`() = runTest {
        every { formRepository.observeForm() } returns flowOf(FORM_PAGES)
        coJustRun { formRepository.refreshForm() }
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as FormUiState.Data

            assertEquals(
                persistentListOf(
                    FormListItem.PageTitle(id = 1, title = "Main Page"),
                    FormListItem.SectionTitle(id = 2, title = "Introduction", depth = 1),
                    FormListItem.TextItem(id = 3, content = "Welcome!", depth = 2),
                    FormListItem.SectionTitle(id = 4, title = "Subsection", depth = 2),
                    FormListItem.ImageItem(
                        id = 5,
                        src = "https://example.com/android.png",
                        title = "Welcome Image",
                        depth = 3,
                    ),
                    FormListItem.ChoiceItem(
                        id = 6,
                        content = "Pick one",
                        multipleSelection = false,
                        options = persistentListOf(
                            ChoiceOption(id = 61, label = "Yes", isSelected = false),
                            ChoiceOption(id = 62, label = "No", isSelected = false),
                        ),
                        depth = 1,
                    ),
                    FormListItem.ChoiceItem(
                        id = 7,
                        content = "Which areas were inspected?",
                        multipleSelection = true,
                        options = persistentListOf(
                            ChoiceOption(id = 71, label = "Entrance", isSelected = false),
                            ChoiceOption(id = 72, label = "Storage", isSelected = false),
                        ),
                        depth = 1,
                    ),
                ),
                state.items,
            )
        }
    }

    @Test
    fun `single selection replaces the previous response and deselects when tapped again`() = runTest {
        every { formRepository.observeForm() } returns flowOf(FORM_PAGES)
        coJustRun { formRepository.refreshForm() }
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            viewModel.onResponseClick(questionId = 6, responseId = 61)
            assertEquals(setOf(61L), expectMostRecentItem().selectedResponseIds(questionId = 6))

            viewModel.onResponseClick(questionId = 6, responseId = 62)
            assertEquals(setOf(62L), expectMostRecentItem().selectedResponseIds(questionId = 6))

            viewModel.onResponseClick(questionId = 6, responseId = 62)
            assertEquals(emptySet<Long>(), expectMostRecentItem().selectedResponseIds(questionId = 6))
        }
    }

    @Test
    fun `multiple selection toggles responses independently`() = runTest {
        every { formRepository.observeForm() } returns flowOf(FORM_PAGES)
        coJustRun { formRepository.refreshForm() }
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            viewModel.onResponseClick(questionId = 7, responseId = 71)
            viewModel.onResponseClick(questionId = 7, responseId = 72)
            assertEquals(setOf(71L, 72L), expectMostRecentItem().selectedResponseIds(questionId = 7))

            viewModel.onResponseClick(questionId = 7, responseId = 71)
            assertEquals(setOf(72L), expectMostRecentItem().selectedResponseIds(questionId = 7))
        }
    }

    @Test
    fun `shows a full screen retry error when refresh fails with an empty cache`() = runTest {
        every { formRepository.observeForm() } returns flowOf(emptyList())
        coEvery { formRepository.refreshForm() } throws IOException("No network")
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            assertEquals(FormUiState.Error(failedAttempts = 1), expectMostRecentItem())

            viewModel.onRetryClick()

            assertEquals(FormUiState.Error(failedAttempts = 2), expectMostRecentItem())
            coVerify(exactly = 2) { formRepository.refreshForm() }
        }
    }

    @Test
    fun `keeps loading until the cache is read even when refresh fails first`() = runTest {
        val cacheFlow = MutableSharedFlow<List<Page>>()
        every { formRepository.observeForm() } returns cacheFlow
        coEvery { formRepository.refreshForm() } throws IOException("No network")
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            assertEquals(FormUiState.Loading, expectMostRecentItem())

            cacheFlow.emit(emptyList())

            assertEquals(FormUiState.Error(failedAttempts = 1), expectMostRecentItem())
        }
    }

    @Test
    fun `shows a refresh indicator while refreshing with cached data`() = runTest {
        every { formRepository.observeForm() } returns flowOf(FORM_PAGES)
        coEvery { formRepository.refreshForm() } coAnswers { awaitCancellation() }
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as FormUiState.Data
            assertTrue(state.showRefreshIndicator)
            assertFalse(state.showRefreshFailedNotice)
        }
    }

    @Test
    fun `shows a dismissible notice when refresh fails with cached data`() = runTest {
        every { formRepository.observeForm() } returns flowOf(FORM_PAGES)
        coEvery { formRepository.refreshForm() } throws IOException("No network")
        val viewModel = FormViewModel(formRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as FormUiState.Data
            assertTrue(state.showRefreshFailedNotice)
            assertTrue(state.items.isNotEmpty())

            viewModel.onRefreshFailedNoticeDismiss()

            assertFalse((expectMostRecentItem() as FormUiState.Data).showRefreshFailedNotice)
        }
    }

    private fun FormUiState.selectedResponseIds(questionId: Long): Set<Long> =
        ((this as FormUiState.Data).items.first { it.id == questionId } as FormListItem.ChoiceItem)
            .options.filter { it.isSelected }.map { it.id }.toSet()

    private companion object {
        val FORM_PAGES = listOf(
            Page(
                id = 1,
                title = "Main Page",
                items = listOf(
                    FormItem.Section(
                        id = 2,
                        title = "Introduction",
                        items = listOf(
                            FormItem.TextQuestion(id = 3, content = "Welcome!"),
                            FormItem.Section(
                                id = 4,
                                title = "Subsection",
                                items = listOf(
                                    FormItem.ImageQuestion(
                                        id = 5,
                                        src = "https://example.com/android.png",
                                        title = "Welcome Image",
                                    ),
                                ),
                            ),
                        ),
                    ),
                    FormItem.ChoiceQuestion(
                        id = 6,
                        content = "Pick one",
                        responseSet = ResponseSet(
                            id = 100,
                            multipleSelection = false,
                            responses = listOf(
                                Response(id = 61, label = "Yes", score = 1),
                                Response(id = 62, label = "No", score = null),
                            ),
                        ),
                    ),
                    FormItem.ChoiceQuestion(
                        id = 7,
                        content = "Which areas were inspected?",
                        responseSet = ResponseSet(
                            id = 101,
                            multipleSelection = true,
                            responses = listOf(
                                Response(id = 71, label = "Entrance", score = null),
                                Response(id = 72, label = "Storage", score = null),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
