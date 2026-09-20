package com.ban.formtree.form.domain.repository

import com.ban.formtree.form.domain.model.Page
import kotlinx.coroutines.flow.Flow

interface FormRepository {

    fun observeForm(): Flow<List<Page>>

    suspend fun refreshForm()
}
