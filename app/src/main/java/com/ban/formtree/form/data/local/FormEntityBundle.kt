package com.ban.formtree.form.data.local

import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity

data class FormEntityBundle(
    val items: List<FormItemEntity>,
    val responseSets: List<ResponseSetEntity>,
    val responses: List<ResponseEntity>,
)
