package com.ban.formtree

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ban.formtree.core.navigation.FormRoute
import com.ban.formtree.core.navigation.ImageDetailsRoute
import com.ban.formtree.form.ui.FormScreen
import com.ban.formtree.imagedetails.ui.ImageDetailsScreen

@Composable
fun FormTreeNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = FormRoute) {
        composable<FormRoute> {
            FormScreen(
                onImageClick = { src, title ->
                    if (navController.currentDestination?.hasRoute<FormRoute>() == true) {
                        navController.navigate(ImageDetailsRoute(src = src, title = title))
                    }
                },
            )
        }
        composable<ImageDetailsRoute> {
            ImageDetailsScreen(
                onCloseClick = {
                    if (navController.currentDestination?.hasRoute<ImageDetailsRoute>() == true) {
                        navController.popBackStack()
                    }
                },
            )
        }
    }
}
