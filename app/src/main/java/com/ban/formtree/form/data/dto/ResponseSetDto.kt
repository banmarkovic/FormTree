package com.ban.formtree.form.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSetDto(
    @SerialName("id") val id: Long,
    @SerialName("multiple_selection") val multipleSelection: Boolean,
    @SerialName("responses") val responses: List<ResponseDto>,
)
