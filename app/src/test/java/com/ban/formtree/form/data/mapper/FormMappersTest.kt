package com.ban.formtree.form.data.mapper

import com.ban.formtree.form.data.dto.FormNodeDto
import com.ban.formtree.form.data.dto.ResponseDto
import com.ban.formtree.form.data.dto.ResponseSetDto
import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.model.Response
import com.ban.formtree.form.domain.model.ResponseSet
import org.junit.Assert.assertEquals
import org.junit.Test

class FormMappersTest {

    @Test
    fun `dto tree flattens to entities and rebuilds into the same domain tree`() {
        val pages = FORM_DTOS.toEntityBundle().toPages()

        assertEquals(EXPECTED_PAGES, pages)
    }

    @Test
    fun `database row order does not affect the rebuilt tree`() {
        val bundle = FORM_DTOS.toEntityBundle()
        val shuffled = bundle.copy(
            items = bundle.items.reversed(),
            responseSets = bundle.responseSets.reversed(),
            responses = bundle.responses.reversed(),
        )

        assertEquals(EXPECTED_PAGES, shuffled.toPages())
    }

    private companion object {
        val FORM_DTOS = listOf(
            FormNodeDto.PageDto(
                id = 1,
                title = "Main Page",
                items = listOf(
                    FormNodeDto.SectionDto(
                        id = 2,
                        title = "Introduction",
                        items = listOf(
                            FormNodeDto.TextDto(id = 3, content = "Welcome!"),
                            FormNodeDto.SectionDto(
                                id = 4,
                                title = "Subsection",
                                items = listOf(
                                    FormNodeDto.ImageDto(
                                        id = 5,
                                        src = "https://example.com/android.png",
                                        title = "Welcome Image",
                                    ),
                                ),
                            ),
                        ),
                    ),
                    FormNodeDto.ChoiceDto(
                        id = 6,
                        content = "Which areas were inspected?",
                        responseSet = ResponseSetDto(
                            id = 101,
                            multipleSelection = true,
                            responses = listOf(
                                ResponseDto(id = 1011, label = "Entrance", score = 1),
                                ResponseDto(id = 1012, label = "Storage", score = null),
                            ),
                        ),
                    ),
                    FormNodeDto.UnknownDto,
                ),
            ),
            FormNodeDto.PageDto(id = 7, title = "Second Page"),
        )

        val EXPECTED_PAGES = listOf(
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
                        content = "Which areas were inspected?",
                        responseSet = ResponseSet(
                            id = 101,
                            multipleSelection = true,
                            responses = listOf(
                                Response(id = 1011, label = "Entrance", score = 1),
                                Response(id = 1012, label = "Storage", score = null),
                            ),
                        ),
                    ),
                ),
            ),
            Page(id = 7, title = "Second Page", items = emptyList()),
        )
    }
}
