package com.ban.formtree.form.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity

data class FormItemWithResponses(
    @Embedded
    val item: FormItemEntity,
    @Relation(
        entity = ResponseSetEntity::class,
        parentColumn = FormItemEntity.COLUMN_ID,
        entityColumn = ResponseSetEntity.COLUMN_FORM_ITEM_ID,
    )
    val responseSet: ResponseSetWithResponses?,
)
