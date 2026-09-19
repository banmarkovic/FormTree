package com.ban.formtree.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.FormItemTypeConverter
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity
import com.ban.formtree.form.data.local.FormDao

@Database(
    entities = [
        FormItemEntity::class,
        ResponseSetEntity::class,
        ResponseEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(FormItemTypeConverter::class)
abstract class FormTreeDatabase : RoomDatabase() {

    abstract fun formDao(): FormDao
}
