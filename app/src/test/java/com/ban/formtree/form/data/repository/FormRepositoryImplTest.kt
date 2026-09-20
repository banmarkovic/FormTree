package com.ban.formtree.form.data.repository

import com.ban.formtree.form.data.dto.FormNodeDto
import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.FormItemType
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity
import com.ban.formtree.form.data.local.FormDao
import com.ban.formtree.form.data.local.FormEntityBundle
import com.ban.formtree.form.data.local.FormItemWithResponses
import com.ban.formtree.form.data.local.ResponseSetWithResponses
import com.ban.formtree.form.data.remote.FormApi
import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.model.Response
import com.ban.formtree.form.domain.model.ResponseSet
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FormRepositoryImplTest {

    private val formApi = mockk<FormApi>()
    private val formDao = mockk<FormDao>()
    private val repository = FormRepositoryImpl(formApi = formApi, formDao = formDao)

    @Test
    fun `refreshForm fetches the form and replaces the cache atomically`() = runTest {
        coEvery { formApi.getForm() } returns listOf(
            FormNodeDto.PageDto(
                id = 1,
                title = "Page",
                items = listOf(FormNodeDto.TextDto(id = 2, content = "Hello")),
            ),
        )
        coJustRun { formDao.replaceAll(any()) }

        repository.refreshForm()

        coVerify {
            formDao.replaceAll(
                FormEntityBundle(
                    items = listOf(
                        FormItemEntity(
                            id = 1,
                            parentFormItemId = null,
                            orderIndex = 0,
                            type = FormItemType.PAGE,
                            title = "Page",
                            content = null,
                            src = null,
                        ),
                        FormItemEntity(
                            id = 2,
                            parentFormItemId = 1,
                            orderIndex = 0,
                            type = FormItemType.TEXT,
                            title = null,
                            content = "Hello",
                            src = null,
                        ),
                    ),
                    responseSets = emptyList(),
                    responses = emptyList(),
                ),
            )
        }
    }

    @Test
    fun `observeForm maps database rows to domain pages`() = runTest {
        every { formDao.observeFormItems() } returns flowOf(
            listOf(
                FormItemWithResponses(
                    item = FormItemEntity(
                        id = 1,
                        parentFormItemId = null,
                        orderIndex = 0,
                        type = FormItemType.PAGE,
                        title = "Page",
                        content = null,
                        src = null,
                    ),
                    responseSet = null,
                ),
                FormItemWithResponses(
                    item = FormItemEntity(
                        id = 2,
                        parentFormItemId = 1,
                        orderIndex = 0,
                        type = FormItemType.CHOICE,
                        title = null,
                        content = "Pick one",
                        src = null,
                    ),
                    responseSet = ResponseSetWithResponses(
                        responseSet = ResponseSetEntity(id = 101, formItemId = 2, multipleSelection = false),
                        responses = listOf(
                            ResponseEntity(
                                id = 1011,
                                responseSetId = 101,
                                orderIndex = 0,
                                label = "Yes",
                                score = 1,
                            ),
                        ),
                    ),
                ),
            ),
        )

        val pages = repository.observeForm().first()

        assertEquals(
            listOf(
                Page(
                    id = 1,
                    title = "Page",
                    items = listOf(
                        FormItem.ChoiceQuestion(
                            id = 2,
                            content = "Pick one",
                            responseSet = ResponseSet(
                                id = 101,
                                multipleSelection = false,
                                responses = listOf(Response(id = 1011, label = "Yes", score = 1)),
                            ),
                        ),
                    ),
                ),
            ),
            pages,
        )
    }
}
