package org.delcom.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class BookResponse(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val genre: String = "Umum",
    val isbn: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val isRead: Boolean = false,
    val cover: String? = null,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
)