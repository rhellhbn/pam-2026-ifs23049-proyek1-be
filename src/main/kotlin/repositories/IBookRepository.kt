package org.delcom.repositories

import org.delcom.entities.Book

interface IBookRepository {
    suspend fun getAll(
        userId: String,
        search: String,
        isRead: Boolean? = null,
        genre: String? = null,
        page: Int = 1,
        perPage: Int = 10
    ): List<Book>

    suspend fun countAll(
        userId: String,
        search: String,
        isRead: Boolean? = null,
        genre: String? = null,
    ): Long

    suspend fun getById(bookId: String): Book?
    suspend fun create(book: Book): String
    suspend fun update(userId: String, bookId: String, newBook: Book): Boolean
    suspend fun delete(userId: String, bookId: String): Boolean
    suspend fun getStats(userId: String): Map<String, Long>
}
