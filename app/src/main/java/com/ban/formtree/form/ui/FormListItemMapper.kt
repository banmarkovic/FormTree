package com.ban.formtree.form.ui

import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun List<Page>.toListItems(
    selectedResponseIdsByQuestionId: Map<Long, Set<Long>>,
): ImmutableList<FormListItem> {
    val listItems = mutableListOf<FormListItem>()
    forEach { page ->
        listItems += FormListItem.PageTitle(id = page.id, title = page.title)
        page.items.forEach {
            collectListItems(
                item = it,
                depth = 1,
                selectedResponseIdsByQuestionId = selectedResponseIdsByQuestionId,
                listItems = listItems,
            )
        }
    }
    return listItems.toImmutableList()
}

private fun collectListItems(
    item: FormItem,
    depth: Int,
    selectedResponseIdsByQuestionId: Map<Long, Set<Long>>,
    listItems: MutableList<FormListItem>,
) {
    when (item) {
        is FormItem.Section -> {
            listItems += FormListItem.SectionTitle(id = item.id, title = item.title, depth = depth)
            item.items.forEach {
                collectListItems(
                    item = it,
                    depth = depth + 1,
                    selectedResponseIdsByQuestionId = selectedResponseIdsByQuestionId,
                    listItems = listItems,
                )
            }
        }

        is FormItem.TextQuestion -> {
            listItems += FormListItem.TextItem(id = item.id, content = item.content, depth = depth)
        }

        is FormItem.ImageQuestion -> {
            listItems += FormListItem.ImageItem(
                id = item.id,
                src = item.src,
                title = item.title,
                depth = depth,
            )
        }

        is FormItem.ChoiceQuestion -> {
            val selectedResponseIds = selectedResponseIdsByQuestionId[item.id].orEmpty()
            listItems += FormListItem.ChoiceItem(
                id = item.id,
                content = item.content,
                multipleSelection = item.responseSet.multipleSelection,
                options = item.responseSet.responses.map { response ->
                    ChoiceOption(
                        id = response.id,
                        label = response.label,
                        isSelected = response.id in selectedResponseIds,
                    )
                }.toImmutableList(),
                depth = depth,
            )
        }
    }
}
