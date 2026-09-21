package com.ban.formtree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ban.formtree.core.ui.theme.FormTreeTheme
import com.ban.formtree.form.ui.FormScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FormTreeTheme {
                FormScreen(onImageClick = { _, _ -> })
            }
        }
    }
}
