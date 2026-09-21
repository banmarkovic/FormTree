package com.ban.formtree.form.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ban.formtree.core.ui.theme.FormTreeTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FormScreen(
    onImageClick: (src: String, title: String) -> Unit,
    viewModel: FormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FormContent(
        uiState = uiState,
        onImageClick = onImageClick,
        onResponseClick = viewModel::onResponseClick,
    )
}

@Composable
private fun FormContent(
    uiState: FormUiState,
    onImageClick: (src: String, title: String) -> Unit,
    onResponseClick: (questionId: Long, responseId: Long) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                items(
                    items = uiState.items,
                    key = { it.id },
                    contentType = { it::class },
                ) { item ->
                    when (item) {
                        is FormListItem.PageTitle -> PageTitle(item = item)
                        is FormListItem.SectionTitle -> SectionTitle(item = item)
                        is FormListItem.TextItem -> TextItem(item = item)
                        is FormListItem.ImageItem -> ImageItem(item = item, onImageClick = onImageClick)
                        is FormListItem.ChoiceItem -> ChoiceItem(item = item, onResponseClick = onResponseClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun PageTitle(item: FormListItem.PageTitle) {
    Text(
        text = item.title,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(start = ITEM_PADDING, top = 24.dp, end = ITEM_PADDING, bottom = 8.dp),
    )
}

@Composable
private fun SectionTitle(item: FormListItem.SectionTitle) {
    Text(
        text = item.title,
        style = sectionTitleStyle(item.depth),
        modifier = Modifier.depthPadding(item.depth).padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun sectionTitleStyle(depth: Int): TextStyle = when {
    depth <= 1 -> MaterialTheme.typography.headlineSmall
    depth == 2 -> MaterialTheme.typography.titleLarge
    depth == 3 -> MaterialTheme.typography.titleMedium
    else -> MaterialTheme.typography.titleSmall
}

@Composable
private fun TextItem(item: FormListItem.TextItem) {
    Text(
        text = item.content,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.depthPadding(item.depth).padding(vertical = 4.dp),
    )
}

@Composable
private fun ImageItem(
    item: FormListItem.ImageItem,
    onImageClick: (src: String, title: String) -> Unit,
) {
    AsyncImage(
        model = item.src,
        contentDescription = item.title,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .depthPadding(item.depth)
            .padding(vertical = 4.dp)
            .height(IMAGE_PREVIEW_HEIGHT)
            .clip(RoundedCornerShape(IMAGE_CORNER_RADIUS))
            .clickable { onImageClick(item.src, item.title) },
    )
}

@Composable
private fun ChoiceItem(
    item: FormListItem.ChoiceItem,
    onResponseClick: (questionId: Long, responseId: Long) -> Unit,
) {
    Column(modifier = Modifier.depthPadding(item.depth).padding(vertical = 4.dp)) {
        Text(
            text = item.content,
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(
            modifier = if (item.multipleSelection) Modifier else Modifier.selectableGroup(),
        ) {
            item.options.forEach { option ->
                ChoiceOptionRow(
                    option = option,
                    multipleSelection = item.multipleSelection,
                    onClick = { onResponseClick(item.id, option.id) },
                )
            }
        }
    }
}

@Composable
private fun ChoiceOptionRow(
    option: ChoiceOption,
    multipleSelection: Boolean,
    onClick: () -> Unit,
) {
    val interactionModifier = if (multipleSelection) {
        Modifier.toggleable(
            value = option.isSelected,
            role = Role.Checkbox,
            onValueChange = { onClick() },
        )
    } else {
        Modifier.selectable(
            selected = option.isSelected,
            role = Role.RadioButton,
            onClick = onClick,
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = interactionModifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MIN_TOUCH_TARGET_SIZE)
            .padding(vertical = 4.dp),
    ) {
        if (multipleSelection) {
            Checkbox(checked = option.isSelected, onCheckedChange = null)
        } else {
            RadioButton(selected = option.isSelected, onClick = null)
        }
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun Modifier.depthPadding(depth: Int): Modifier =
    padding(start = ITEM_PADDING + DEPTH_INDENT * depth.coerceAtMost(MAX_INDENT_DEPTH), end = ITEM_PADDING)

private const val MAX_INDENT_DEPTH = 5
private val ITEM_PADDING = 16.dp
private val DEPTH_INDENT = 12.dp
private val IMAGE_PREVIEW_HEIGHT = 120.dp
private val IMAGE_CORNER_RADIUS = 12.dp
private val MIN_TOUCH_TARGET_SIZE = 48.dp

@Preview(showBackground = true)
@Composable
private fun FormContentPreview() {
    FormTreeTheme {
        FormContent(
            uiState = FormUiState(
                isLoading = false,
                items = persistentListOf(
                    FormListItem.PageTitle(id = 1, title = "Main Page"),
                    FormListItem.SectionTitle(id = 2, title = "Introduction", depth = 1),
                    FormListItem.TextItem(id = 3, content = "Welcome to the main page!", depth = 2),
                    FormListItem.SectionTitle(id = 4, title = "Subsection 1.1", depth = 2),
                    FormListItem.TextItem(id = 5, content = "This is a subsection.", depth = 3),
                    FormListItem.ChoiceItem(
                        id = 6,
                        content = "Which areas were inspected?",
                        multipleSelection = true,
                        options = persistentListOf(
                            ChoiceOption(id = 61, label = "Entrance", isSelected = false),
                            ChoiceOption(id = 62, label = "Storage", isSelected = true),
                        ),
                        depth = 2,
                    ),
                ),
            ),
            onImageClick = { _, _ -> },
            onResponseClick = { _, _ -> },
        )
    }
}
