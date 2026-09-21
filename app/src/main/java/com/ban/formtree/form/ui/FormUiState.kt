package com.ban.formtree.form.ui

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class FormUiState(
    val isLoading: Boolean = true,
    val items: ImmutableList<FormListItem> = persistentListOf(),
)

@Immutable
sealed interface FormListItem {
    val id: Long

    @Immutable
    data class PageTitle(
        override val id: Long,
        val title: String,
    ) : FormListItem

    @Immutable
    data class SectionTitle(
        override val id: Long,
        val title: String,
        val depth: Int,
    ) : FormListItem

    @Immutable
    data class TextItem(
        override val id: Long,
        val content: String,
        val depth: Int,
    ) : FormListItem

    @Immutable
    data class ImageItem(
        override val id: Long,
        val src: String,
        val title: String,
        val depth: Int,
    ) : FormListItem

    @Immutable
    data class ChoiceItem(
        override val id: Long,
        val content: String,
        val multipleSelection: Boolean,
        val options: ImmutableList<ChoiceOption>,
        val depth: Int,
    ) : FormListItem
}

@Immutable
data class ChoiceOption(
    val id: Long,
    val label: String,
    val isSelected: Boolean,
)

