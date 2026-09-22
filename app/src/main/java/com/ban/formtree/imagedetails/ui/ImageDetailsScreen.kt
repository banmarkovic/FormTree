package com.ban.formtree.imagedetails.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ban.formtree.R
import com.ban.formtree.core.ui.theme.FormTreeTheme

@Composable
fun ImageDetailsScreen(
    onCloseClick: () -> Unit,
    viewModel: ImageDetailsViewModel = hiltViewModel(),
) {
    ImageDetailsContent(uiState = viewModel.uiState, onCloseClick = onCloseClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageDetailsContent(
    uiState: ImageDetailsUiState,
    onCloseClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.displayCutout),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AsyncImage(
            model = uiState.src,
            contentDescription = uiState.title,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageDetailsContentPreview() {
    FormTreeTheme {
        ImageDetailsContent(
            uiState = ImageDetailsUiState(
                src = "https://example.com/android.png",
                title = "Welcome Image",
            ),
            onCloseClick = {},
        )
    }
}
