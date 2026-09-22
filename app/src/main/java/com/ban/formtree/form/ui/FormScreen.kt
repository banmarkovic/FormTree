package com.ban.formtree.form.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                            ChoiceOption(id = 61, label = "Entrance", isSelected = false),
                            ChoiceOption(id = 62, label = "Storage", isSelected = true),
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
