package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class BookResponse(
    val id: String,
    val userId: String,
    val title: String,
    val author: String,
    val description: String,
    val genre: String,
    val isbn: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val isRead: Boolean,
    val cover: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BooksResponse(
    val books: List<BookResponse>,
    val total: Long,
    val page: Int,
    val perPage: Int
)

@Serializable
data class BookStatsResponse(
    val total: Long,
    val read: Long,
    val unread: Long
)