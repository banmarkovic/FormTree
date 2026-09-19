package com.ban.formtree.form.domain.model

sealed interface FormItem {
    val id: Long

    data class Section(
        override val id: Long,
        val title: String,
        val items: List<FormItem>,
    ) : FormItem

    data class TextQuestion(
        override val id: Long,
        val content: String,
    ) : FormItem

    data class ImageQuestion(
        override val id: Long,
        val src: String,
        val title: String,
    ) : FormItem

    data class ChoiceQuestion(
        override val id: Long,
        val content: String,
        val responseSet: ResponseSet,
    ) : FormItem
}
