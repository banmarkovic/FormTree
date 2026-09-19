package com.ban.formtree.form.domain.model

data class Page(
    val id: Long,
    val title: String,
    val items: List<FormItem>,
)
