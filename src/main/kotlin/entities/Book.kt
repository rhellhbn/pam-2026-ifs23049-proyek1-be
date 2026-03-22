package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Book(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var title: String,
    var author: String,
    var description: String,
    var genre: String = "Umum",
    var isbn: String? = null,
    var publisher: String? = null,
    var year: Int? = null,
    var cover: String? = null,
    var isRead: Boolean = false,
    // Ubah dari Instant ke String
    val createdAt: String = Clock.System.now().toString(),
    var updatedAt: String = Clock.System.now().toString(),
)