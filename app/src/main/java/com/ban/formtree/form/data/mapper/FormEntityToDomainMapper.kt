package com.ban.formtree.form.data.mapper

import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.FormItemType
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity
import com.ban.formtree.form.data.local.FormEntityBundle
import com.ban.formtree.form.domain.model.FormItem
import com.ban.formtree.form.domain.model.Page
import com.ban.formtree.form.domain.model.Response
import com.ban.formtree.form.domain.model.ResponseSet

fun FormEntityBundle.toPages(): List<Page> {
    val childrenByParentId = items
        .filter { it.parentFormItemId != null }
        .sortedBy { it.orderIndex }
        .groupBy { it.parentFormItemId }
    val responseSetsByItemId = responseSets.associateBy { it.formItemId }
    val responsesBySetId = responses
        .sortedBy { it.orderIndex }
        .groupBy { it.responseSetId }

    return items
        .filter { it.parentFormItemId == null && it.type == FormItemType.PAGE }
        .sortedBy { it.orderIndex }
        .map { page ->
            Page(
                id = page.id,
                title = requireNotNull(page.title) { "Page ${page.id} has no title" },
                items = childrenByParentId[page.id].orEmpty().map { child ->
                    toFormItem(
                        entity = child,
                        childrenByParentId = childrenByParentId,
                        responseSetsByItemId = responseSetsByItemId,
                        responsesBySetId = responsesBySetId,
                    )
                },
            )
        }
}

private fun toFormItem(
    entity: FormItemEntity,
    childrenByParentId: Map<Long?, List<FormItemEntity>>,
    responseSetsByItemId: Map<Long, ResponseSetEntity>,
    responsesBySetId: Map<Long, List<ResponseEntity>>,
): FormItem = when (entity.type) {
    FormItemType.SECTION -> FormItem.Section(
        id = entity.id,
        title = requireNotNull(entity.title) { "Section ${entity.id} has no title" },
        items = childrenByParentId[entity.id].orEmpty().map { child ->
            toFormItem(
                entity = child,
                childrenByParentId = childrenByParentId,
                responseSetsByItemId = responseSetsByItemId,
                responsesBySetId = responsesBySetId,
            )
        },
    )

    FormItemType.TEXT -> FormItem.TextQuestion(
        id = entity.id,
        content = requireNotNull(entity.content) { "Text question ${entity.id} has no content" },
    )

    FormItemType.IMAGE -> FormItem.ImageQuestion(
        id = entity.id,
        src = requireNotNull(entity.src) { "Image question ${entity.id} has no src" },
        title = requireNotNull(entity.title) { "Image question ${entity.id} has no title" },
    )

    FormItemType.CHOICE -> {
        val responseSet = requireNotNull(responseSetsByItemId[entity.id]) {
            "Choice question ${entity.id} has no response set"
        }
        FormItem.ChoiceQuestion(
            id = entity.id,
            content = requireNotNull(entity.content) { "Choice question ${entity.id} has no content" },
            responseSet = ResponseSet(
                id = responseSet.id,
                multipleSelection = responseSet.multipleSelection,
                responses = responsesBySetId[responseSet.id].orEmpty().map {
                    Response(id = it.id, label = it.label, score = it.score)
                },
            ),
        )
    }

    FormItemType.PAGE -> throw IllegalStateException("Page ${entity.id} cannot be nested inside another item")
}
