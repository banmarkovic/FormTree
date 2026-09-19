package com.ban.formtree.core.database.di

import android.content.Context
import androidx.room.Room
import com.ban.formtree.core.database.FormTreeDatabase
import com.ban.formtree.form.data.local.FormDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "formtree.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FormTreeDatabase =
        Room.databaseBuilder(
            context = context,
            klass = FormTreeDatabase::class.java,
            name = DATABASE_NAME,
        ).build()

    @Provides
    fun provideFormDao(database: FormTreeDatabase): FormDao = database.formDao()
}
