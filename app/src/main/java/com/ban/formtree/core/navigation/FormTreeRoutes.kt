package com.ban.formtree.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object FormRoute

@Serializable
data class ImageDetailsRoute(
    val src: String,
    val title: String,
)
