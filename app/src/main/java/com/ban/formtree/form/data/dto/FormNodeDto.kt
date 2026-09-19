package com.ban.formtree.form.data.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface FormNodeDto {

    @Serializable
    @SerialName("page")
    data class PageDto(
        @SerialName("id") val id: Long,
        @SerialName("title") val title: String,
        @SerialName("items") val items: List<FormNodeDto> = emptyList(),
    ) : FormNodeDto

    @Serializable
    @SerialName("section")
    data class SectionDto(
        @SerialName("id") val id: Long,
        @SerialName("title") val title: String,
        @SerialName("items") val items: List<FormNodeDto> = emptyList(),
    ) : FormNodeDto

    @Serializable
    @SerialName("text")
    data class TextDto(
        @SerialName("id") val id: Long,
        @SerialName("content") val content: String,
    ) : FormNodeDto

    @Serializable
    @SerialName("image")
    data class ImageDto(
        @SerialName("id") val id: Long,
        @SerialName("src") val src: String,
        @SerialName("title") val title: String,
    ) : FormNodeDto

    @Serializable
    @SerialName("choice")
    data class ChoiceDto(
        @SerialName("id") val id: Long,
        @SerialName("content") val content: String,
        @SerialName("response_set") val responseSet: ResponseSetDto,
    ) : FormNodeDto

    @Serializable
    data object UnknownDto : FormNodeDto
}
