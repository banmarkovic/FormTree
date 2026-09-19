package com.ban.formtree.form.data.di

import com.ban.formtree.form.data.remote.FormApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object FormNetworkModule {

    @Provides
    @Singleton
    fun provideFormApi(retrofit: Retrofit): FormApi = retrofit.create()
}
