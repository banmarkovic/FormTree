package com.ban.formtree.form.data.mapper

import com.ban.formtree.form.data.dto.FormNodeDto
import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.FormItemType
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity
import com.ban.formtree.form.data.local.FormEntityBundle

fun List<FormNodeDto>.toEntityBundle(): FormEntityBundle {
    val items = mutableListOf<FormItemEntity>()
    val responseSets = mutableListOf<ResponseSetEntity>()
    val responses = mutableListOf<ResponseEntity>()
    forEachIndexed { index, node ->
        collectEntities(
            node = node,
            parentId = null,
            orderIndex = index,
            items = items,
            responseSets = responseSets,
            responses = responses
        )
    }
    return FormEntityBundle(items = items, responseSets = responseSets, responses = responses)
}

private fun collectEntities(
    node: FormNodeDto,
    parentId: Long?,
    orderIndex: Int,
    items: MutableList<FormItemEntity>,
    responseSets: MutableList<ResponseSetEntity>,
    responses: MutableList<ResponseEntity>,
) {
    when (node) {
        is FormNodeDto.PageDto -> {
            items += FormItemEntity(
                id = node.id,
                parentFormItemId = parentId,
                orderIndex = orderIndex,
                type = FormItemType.PAGE,
                title = node.title,
                content = null,
                src = null,
            )
            node.items.forEachIndexed { index, child ->
                collectEntities(
                    node = child,
                    parentId = node.id,
                    orderIndex = index,
                    items = items,
                    responseSets = responseSets,
                    responses = responses,
                )
            }
        }

        is FormNodeDto.SectionDto -> {
            items += FormItemEntity(
                id = node.id,
                parentFormItemId = parentId,
                orderIndex = orderIndex,
                type = FormItemType.SECTION,
                title = node.title,
                content = null,
                src = null,
            )
            node.items.forEachIndexed { index, child ->
                collectEntities(
                    node = child,
                    parentId = node.id,
                    orderIndex = index,
                    items = items,
                    responseSets = responseSets,
                    responses = responses,
                )
            }
        }

        is FormNodeDto.TextDto -> {
            items += FormItemEntity(
                id = node.id,
                parentFormItemId = parentId,
                orderIndex = orderIndex,
                type = FormItemType.TEXT,
                title = null,
                content = node.content,
                src = null,
            )
        }

        is FormNodeDto.ImageDto -> {
            items += FormItemEntity(
                id = node.id,
                parentFormItemId = parentId,
                orderIndex = orderIndex,
                type = FormItemType.IMAGE,
                title = node.title,
                content = null,
                src = node.src,
            )
        }

        is FormNodeDto.ChoiceDto -> {
            items += FormItemEntity(
                id = node.id,
                parentFormItemId = parentId,
                orderIndex = orderIndex,
                type = FormItemType.CHOICE,
                title = null,
                content = node.content,
                src = null,
            )
            responseSets += ResponseSetEntity(
                id = node.responseSet.id,
                formItemId = node.id,
                multipleSelection = node.responseSet.multipleSelection,
            )
            node.responseSet.responses.forEachIndexed { index, response ->
                responses += ResponseEntity(
                    id = response.id,
                    responseSetId = node.responseSet.id,
                    orderIndex = index,
                    label = response.label,
                    score = response.score,
                )
            }
        }
    }
}
