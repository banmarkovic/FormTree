package com.ban.formtree.form.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ban.formtree.R
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
        onRetryClick = viewModel::onRetryClick,
        onRefreshFailedNoticeDismiss = viewModel::onRefreshFailedNoticeDismiss,
    )
}

@Composable
private fun FormContent(
    uiState: FormUiState,
    onImageClick: (src: String, title: String) -> Unit,
    onResponseClick: (questionId: Long, responseId: Long) -> Unit,
    onRetryClick: () -> Unit,
    onRefreshFailedNoticeDismiss: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val failedAttempts = when (uiState) {
        is FormUiState.Error -> uiState.failedRefreshAttempts
        is FormUiState.Data -> uiState.failedRefreshAttempts
        FormUiState.Loading -> 0
    }
    var lastAnnouncedAttempt by rememberSaveable { mutableIntStateOf(1) }
    val retryFailedMessage = stringResource(R.string.refresh_failed_notice)
    LaunchedEffect(failedAttempts) {
        val isRepeatedFailure = failedAttempts > lastAnnouncedAttempt
        lastAnnouncedAttempt = maxOf(failedAttempts, 1)
        if (isRepeatedFailure) {
            snackbarHostState.showSnackbar(retryFailedMessage)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            (uiState as? FormUiState.Data)?.let { data ->
                when {
                    data.failedRefreshAttempts > 0 -> RefreshFailedNoticeBanner(
                        onRetryClick = onRetryClick,
                        onDismissClick = onRefreshFailedNoticeDismiss,
                    )

                    data.showRefreshIndicator -> RefreshIndicator()
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState,
            contentKey = { it::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { state ->
            when (state) {
                FormUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is FormUiState.Error -> {
                    RetryError(
                        onRetryClick = onRetryClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }

                is FormUiState.Data -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = innerPadding,
                    ) {
                        items(
                            items = state.items,
                            key = { it.id },
                            contentType = { it::class },
                        ) { item ->
                            when (item) {
                                is FormListItem.PageTitle -> PageTitle(item = item)
                                is FormListItem.SectionTitle -> SectionTitle(item = item)
                                is FormListItem.TextItem -> TextItem(item = item)
                                is FormListItem.ImageItem -> ImageItem(item = item, onImageClick = onImageClick)
                                is FormListItem.ChoiceItem -> ChoiceItem(item = item, onResponseClick = onResponseClick)
                                is FormListItem.BlockSpacer -> BlockSpacer(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshFailedNoticeBanner(
    onRetryClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.statusBars.union(
                        WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
                    ),
                )
                .padding(start = ITEM_PADDING, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.saved_data_notice),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetryClick) {
                Text(text = stringResource(R.string.retry))
            }
            IconButton(onClick = onDismissClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_24),
                    contentDescription = stringResource(R.string.dismiss),
                )
            }
        }
    }
}

@Composable
private fun RefreshIndicator() {
    LinearProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.statusBars.union(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
                ),
            ),
    )
}

@Composable
private fun RetryError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = ITEM_PADDING),
    ) {
        Text(
            text = stringResource(R.string.form_load_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun PageTitle(item: FormListItem.PageTitle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = PAGE_SPACING)
            .depthContent(depth = 0)
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(modifier = Modifier.width(8.dp))
        IdBadge(id = item.id)
    }
}

@Composable
private fun SectionTitle(item: FormListItem.SectionTitle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .sectionHeaderContent(item.depth)
            .padding(top = 12.dp, bottom = 4.dp),
    ) {
        Text(
            text = item.title,
            style = sectionTitleStyle(item.depth),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(modifier = Modifier.width(8.dp))
        IdBadge(id = item.id)
    }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .depthContent(depth = item.depth, backgroundDepth = item.depth - 1)
            .padding(vertical = ITEM_VERTICAL_SPACING),
    ) {
        Text(
            text = item.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(modifier = Modifier.width(8.dp))
        IdBadge(id = item.id)
    }
}

@Composable
private fun ImageItem(
    item: FormListItem.ImageItem,
    onImageClick: (src: String, title: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .depthContent(depth = item.depth, backgroundDepth = item.depth - 1)
            .padding(vertical = ITEM_VERTICAL_SPACING),
    ) {
        AsyncImage(
            model = item.src,
            contentDescription = item.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(IMAGE_PREVIEW_HEIGHT)
                .clip(RoundedCornerShape(IMAGE_CORNER_RADIUS))
                .clickable { onImageClick(item.src, item.title) },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            IdBadge(id = item.id)
        }
    }
}

@Composable
private fun ChoiceItem(
    item: FormListItem.ChoiceItem,
    onResponseClick: (questionId: Long, responseId: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .depthContent(depth = item.depth, backgroundDepth = item.depth - 1)
            .padding(vertical = ITEM_VERTICAL_SPACING),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IdBadge(id = item.id)
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = if (item.multipleSelection) Modifier else Modifier.selectableGroup(),
        ) {
            item.options.forEach { option ->
                ChoiceChip(
                    option = option,
                    multipleSelection = item.multipleSelection,
                    onClick = { onResponseClick(item.id, option.id) },
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        SelectionCaption(item = item)
    }
}

@Composable
private fun ChoiceChip(
    option: ChoiceOption,
    multipleSelection: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(percent = 50)
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
    Surface(
        shape = shape,
        color = if (option.isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (option.isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = if (option.isSelected) {
            null
        } else {
            BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .then(interactionModifier),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
            )
            option.score?.let { score ->
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionCaption(item: FormListItem.ChoiceItem) {
    val selectedCount = item.options.count { it.isSelected }
    val countText = if (selectedCount == 0) {
        stringResource(R.string.selection_none)
    } else {
        stringResource(R.string.selection_count, selectedCount)
    }
    val typeText = stringResource(
        if (item.multipleSelection) R.string.selection_multi else R.string.selection_single,
    )
    Text(
        text = stringResource(R.string.selection_caption, countText, typeText),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun BlockSpacer(item: FormListItem.BlockSpacer) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(BLOCK_END_SPACING)
            .hierarchyBands(levels = 0 until item.depth.coerceAtMost(MAX_INDENT_DEPTH)),
    )
}

@Composable
private fun IdBadge(id: Long) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = stringResource(R.string.item_id_badge, id),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun Modifier.hierarchyBands(levels: IntRange): Modifier {
    val tint = MaterialTheme.colorScheme.primary.copy(alpha = HIERARCHY_TINT_ALPHA)
    return drawBehind {
        for (level in levels) {
            val startX = if (level == 0) {
                0f
            } else {
                ITEM_PADDING.toPx() + (level - 1) * DEPTH_INDENT.toPx()
            }
            drawRect(
                color = tint,
                topLeft = Offset(startX, 0f),
                size = Size(width = size.width - startX, height = size.height),
            )
        }
    }
}

@Composable
private fun Modifier.depthContent(depth: Int, backgroundDepth: Int = depth): Modifier {
    val clampedDepth = depth.coerceAtMost(MAX_INDENT_DEPTH)
    return hierarchyBands(levels = 0..backgroundDepth.coerceAtMost(MAX_INDENT_DEPTH))
        .padding(start = ITEM_PADDING + DEPTH_INDENT * clampedDepth, end = ITEM_PADDING)
}

@Composable
private fun Modifier.sectionHeaderContent(depth: Int): Modifier {
    val clampedDepth = depth.coerceAtMost(MAX_INDENT_DEPTH)
    return hierarchyBands(levels = 0 until clampedDepth)
        .padding(top = SECTION_SPACING)
        .hierarchyBands(levels = clampedDepth..clampedDepth)
        .padding(start = ITEM_PADDING + DEPTH_INDENT * clampedDepth, end = ITEM_PADDING)
}

private const val MAX_INDENT_DEPTH = 5
private const val HIERARCHY_TINT_ALPHA = 0.1f
private val ITEM_PADDING = 16.dp
private val DEPTH_INDENT = 14.dp
private val PAGE_SPACING = 16.dp
private val SECTION_SPACING = 12.dp
private val BLOCK_END_SPACING = 12.dp
private val ITEM_VERTICAL_SPACING = 12.dp
private val IMAGE_PREVIEW_HEIGHT = 120.dp
private val IMAGE_CORNER_RADIUS = 12.dp

@Preview(showBackground = true)
@Composable
private fun FormContentPreview() {
    FormTreeTheme {
        FormContent(
            uiState = FormUiState.Data(
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
                            ChoiceOption(id = 61, label = "Entrance", score = 1, isSelected = false),
                            ChoiceOption(id = 62, label = "Storage", score = null, isSelected = true),
                        ),
                        depth = 2,
                    ),
                ),
            ),
            onImageClick = { _, _ -> },
            onResponseClick = { _, _ -> },
            onRetryClick = {},
            onRefreshFailedNoticeDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RetryErrorPreview() {
    FormTreeTheme {
        FormContent(
            uiState = FormUiState.Error(failedRefreshAttempts = 1),
            onImageClick = { _, _ -> },
            onResponseClick = { _, _ -> },
            onRetryClick = {},
            onRefreshFailedNoticeDismiss = {},
        )
    }
}
