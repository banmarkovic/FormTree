package com.ban.formtree.form.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = ResponseSetEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = FormItemEntity::class,
            parentColumns = [FormItemEntity.COLUMN_ID],
            childColumns = [ResponseSetEntity.COLUMN_FORM_ITEM_ID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(ResponseSetEntity.COLUMN_FORM_ITEM_ID)],
)
data class ResponseSetEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,
    @ColumnInfo(name = COLUMN_FORM_ITEM_ID)
    val formItemId: Long,
    @ColumnInfo(name = COLUMN_MULTIPLE_SELECTION)
    val multipleSelection: Boolean,
) {
    companion object {
        const val TABLE_NAME = "response_sets"
        const val COLUMN_ID = "id"
        const val COLUMN_FORM_ITEM_ID = "form_item_id"
        const val COLUMN_MULTIPLE_SELECTION = "multiple_selection"
    }
}
