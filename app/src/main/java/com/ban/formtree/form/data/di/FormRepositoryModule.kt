package com.ban.formtree.form.data.di

import com.ban.formtree.form.data.repository.FormRepositoryImpl
import com.ban.formtree.form.domain.repository.FormRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface FormRepositoryModule {

    @Binds
    @Singleton
    fun bindFormRepository(impl: FormRepositoryImpl): FormRepository
}
