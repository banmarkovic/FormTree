package com.ban.formtree.form.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = FormItemEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = FormItemEntity::class,
            parentColumns = [FormItemEntity.COLUMN_ID],
            childColumns = [FormItemEntity.COLUMN_PARENT_FORM_ITEM_ID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(FormItemEntity.COLUMN_PARENT_FORM_ITEM_ID)],
)
data class FormItemEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,
    @ColumnInfo(name = COLUMN_PARENT_FORM_ITEM_ID)
    val parentFormItemId: Long?,
    @ColumnInfo(name = COLUMN_ORDER_INDEX)
    val orderIndex: Int,
    @ColumnInfo(name = COLUMN_TYPE)
    val type: FormItemType,
    @ColumnInfo(name = COLUMN_TITLE)
    val title: String?,
    @ColumnInfo(name = COLUMN_CONTENT)
    val content: String?,
    @ColumnInfo(name = COLUMN_SRC)
    val src: String?,
) {
    companion object {
        const val TABLE_NAME = "form_items"
        const val COLUMN_ID = "id"
        const val COLUMN_PARENT_FORM_ITEM_ID = "parent_form_item_id"
        const val COLUMN_ORDER_INDEX = "order_index"
        const val COLUMN_TYPE = "type"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_SRC = "src"
    }
}
