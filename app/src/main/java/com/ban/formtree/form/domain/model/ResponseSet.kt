package com.ban.formtree.form.domain.model

data class ResponseSet(
    val id: Long,
    val multipleSelection: Boolean,
    val responses: List<Response>,
)
