package com.ban.formtree.form.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity

data class ResponseSetWithResponses(
    @Embedded
    val responseSet: ResponseSetEntity,
    @Relation(
        parentColumn = ResponseSetEntity.COLUMN_ID,
        entityColumn = ResponseEntity.COLUMN_RESPONSE_SET_ID,
    )
    val responses: List<ResponseEntity>,
)
