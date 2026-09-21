package com.ban.formtree.form.ui

import app.cash.turbine.test
import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.model.Response
import com.ban.formtree.form.domain.model.ResponseSet
import com.ban.formtree.form.domain.repository.FormRepository
import com.ban.formtree.testing.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            val state = expectMostRecentItem()

            assertFalse(state.isLoading)
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
                ),
                state.items,
            )
        }
    }

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
                ),
            ),
        )
    }
}
