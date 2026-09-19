package com.ban.formtree.form.data.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class FormItemTypeConverterTest {

    private val converter = FormItemTypeConverter()

    @Test
    fun `every type maps to a stable storage string and back`() {
        val expectedStorageStrings = mapOf(
            FormItemType.PAGE to "page",
            FormItemType.SECTION to "section",
            FormItemType.TEXT to "text",
            FormItemType.IMAGE to "image",
            FormItemType.CHOICE to "choice",
        )

        FormItemType.entries.forEach { type ->
            val stored = converter.fromFormItemType(type)
            assertEquals(expectedStorageStrings.getValue(type), stored)
            assertEquals(type, converter.toFormItemType(stored))
        }
    }
}
