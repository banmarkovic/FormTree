package com.ban.formtree.form.data.repository

import com.ban.formtree.form.data.local.FormDao
import com.ban.formtree.form.data.local.FormEntityBundle
import com.ban.formtree.form.data.mapper.toEntityBundle
import com.ban.formtree.form.data.mapper.toPages
import com.ban.formtree.form.data.remote.FormApi
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.repository.FormRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FormRepositoryImpl @Inject constructor(
    private val formApi: FormApi,
    private val formDao: FormDao,
) : FormRepository {

    override fun observeForm(): Flow<List<Page>> = formDao.observeFormItems()
        .map { itemsWithResponses ->
            FormEntityBundle(
                items = itemsWithResponses.map { it.item },
                responseSets = itemsWithResponses.mapNotNull { it.responseSet?.responseSet },
                responses = itemsWithResponses.flatMap { it.responseSet?.responses.orEmpty() },
            ).toPages()
        }
        .distinctUntilChanged()

    override suspend fun refreshForm() {
        formDao.replaceAll(formApi.getForm().toEntityBundle())
    }
}
