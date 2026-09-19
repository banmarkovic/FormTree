package com.ban.formtree.form.data.entity

import androidx.room.TypeConverter

class FormItemTypeConverter {

    @TypeConverter
    fun fromFormItemType(type: FormItemType): String = when (type) {
        FormItemType.PAGE -> TYPE_PAGE
        FormItemType.SECTION -> TYPE_SECTION
        FormItemType.TEXT -> TYPE_TEXT
        FormItemType.IMAGE -> TYPE_IMAGE
        FormItemType.CHOICE -> TYPE_CHOICE
    }

    @TypeConverter
    fun toFormItemType(value: String): FormItemType = when (value) {
        TYPE_PAGE -> FormItemType.PAGE
        TYPE_SECTION -> FormItemType.SECTION
        TYPE_TEXT -> FormItemType.TEXT
        TYPE_IMAGE -> FormItemType.IMAGE
        TYPE_CHOICE -> FormItemType.CHOICE
        else -> throw IllegalArgumentException("Unknown form item type stored in database: $value")
    }

    private companion object {
        const val TYPE_PAGE = "page"
        const val TYPE_SECTION = "section"
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
        const val TYPE_CHOICE = "choice"
    }
}
