package com.ban.formtree.form.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseDto(
    @SerialName("id") val id: Long,
    @SerialName("label") val label: String,
    @SerialName("score") val score: Int? = null,
)
