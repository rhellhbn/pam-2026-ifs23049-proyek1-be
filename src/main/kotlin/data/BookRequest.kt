package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Book

@Serializable
data class BookRequest(
    var userId: String = "",
    var title: String = "",
    var author: String = "",
    var description: String = "",
    var genre: String = "Umum",
    var isbn: String? = null,
    var publisher: String? = null,
    var year: Int? = null,
    var cover: String? = null,
    var isRead: Boolean = false,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"      to userId,
        "title"       to title,
        "author"      to author,
        "description" to description,
        "genre"       to genre,
        "isbn"        to isbn,
        "publisher"   to publisher,
        "year"        to year,
        "cover"       to cover,
        "isRead"      to isRead,
    )

    fun toEntity(): Book = Book(
        userId      = userId,
        title       = title,
        author      = author,
        description = description,
        genre       = genre,
        isbn        = isbn,
        publisher   = publisher,
        year        = year,
        cover       = cover,
        isRead      = isRead,
        updatedAt   = Clock.System.now()  // ✅ hapus .toString()
    )
}