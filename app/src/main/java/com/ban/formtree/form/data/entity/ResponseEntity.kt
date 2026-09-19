package com.ban.formtree.form.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = ResponseEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = ResponseSetEntity::class,
            parentColumns = [ResponseSetEntity.COLUMN_ID],
            childColumns = [ResponseEntity.COLUMN_RESPONSE_SET_ID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(ResponseEntity.COLUMN_RESPONSE_SET_ID)],
)
data class ResponseEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,
    @ColumnInfo(name = COLUMN_RESPONSE_SET_ID)
    val responseSetId: Long,
    @ColumnInfo(name = COLUMN_ORDER_INDEX)
    val orderIndex: Int,
    @ColumnInfo(name = COLUMN_LABEL)
    val label: String,
    @ColumnInfo(name = COLUMN_SCORE)
    val score: Int?,
) {
    companion object {
        const val TABLE_NAME = "responses"
        const val COLUMN_ID = "id"
        const val COLUMN_RESPONSE_SET_ID = "response_set_id"
        const val COLUMN_ORDER_INDEX = "order_index"
        const val COLUMN_LABEL = "label"
        const val COLUMN_SCORE = "score"
    }
}
