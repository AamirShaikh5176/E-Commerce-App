package com.example.e_commerceapp.data.dto.firestore

import com.example.e_commerceapp.data.dto.Category

/**
 * Data transfer object representing a Category document in Firestore (/categories/{id}).
 * Contains default parameter values to support Firebase Firestore automatic deserialization.
 */
data class FirestoreCategoryDto(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val url: String = "",
    val imageUrl: String = "",
) {
    fun toCategory(): Category {
        val resolvedName = name.ifBlank { slug.replaceFirstChar { it.uppercase() } }
        val resolvedSlug = slug.ifBlank { id }
        return Category(
            name = resolvedName,
            slug = resolvedSlug,
            url = url,
        )
    }
}
