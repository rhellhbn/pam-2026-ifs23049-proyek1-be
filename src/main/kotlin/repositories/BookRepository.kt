package org.delcom.repositories

import kotlinx.datetime.Clock
import org.delcom.dao.BookDAO
import org.delcom.entities.Book
import org.delcom.helpers.bookDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.BookTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.util.UUID

class BookRepository : IBookRepository {

    private fun buildFilter(
        userId: String,
        search: String,
        isRead: Boolean?,
        genre: String?,
    ): Op<Boolean> {
        val userUuid = UUID.fromString(userId)
        var op: Op<Boolean> = BookTable.userId eq userUuid

        if (search.isNotBlank()) {
            op = op and (
                    (BookTable.title.lowerCase() like "%${search.lowercase()}%") or
                            (BookTable.author.lowerCase() like "%${search.lowercase()}%")
                    )
        }
        if (isRead != null) {
            op = op and (BookTable.isRead eq isRead)
        }
        if (!genre.isNullOrBlank()) {
            op = op and (BookTable.genre eq genre)
        }
        return op
    }

    override suspend fun getAll(
        userId: String,
        search: String,
        isRead: Boolean?,
        genre: String?,
        page: Int,
        perPage: Int
    ): List<Book> = suspendTransaction {
        val offset = ((page - 1) * perPage).toLong()
        BookDAO.find { buildFilter(userId, search, isRead, genre) }
            .orderBy(
                if (search.isNotBlank()) BookTable.title to SortOrder.ASC
                else BookTable.createdAt to SortOrder.DESC
            )
            .limit(perPage)
            .offset(offset)
            .map(::bookDAOToModel)
    }

    override suspend fun countAll(
        userId: String,
        search: String,
        isRead: Boolean?,
        genre: String?,
    ): Long = suspendTransaction {
        BookDAO.find { buildFilter(userId, search, isRead, genre) }.count()
    }

    override suspend fun getById(bookId: String): Book? = suspendTransaction {
        BookDAO.find { BookTable.id eq UUID.fromString(bookId) }
            .limit(1)
            .map(::bookDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(book: Book): String = suspendTransaction {
        BookDAO.new {
            userId      = UUID.fromString(book.userId)
            title       = book.title
            author      = book.author
            description = book.description
            genre       = book.genre
            isbn        = book.isbn
            publisher   = book.publisher
            year        = book.year
            cover       = book.cover
            isRead      = book.isRead
            createdAt   = Clock.System.now()  // ← wajib diisi
            updatedAt   = Clock.System.now()  // ← wajib diisi
        }.id.value.toString()
    }

    override suspend fun update(userId: String, bookId: String, newBook: Book): Boolean = suspendTransaction {
        val dao = BookDAO.find {
            (BookTable.id eq UUID.fromString(bookId)) and
                    (BookTable.userId eq UUID.fromString(userId))
        }.limit(1).firstOrNull()

        if (dao != null) {
            dao.title       = newBook.title
            dao.author      = newBook.author
            dao.description = newBook.description
            dao.genre       = newBook.genre
            dao.isbn        = newBook.isbn
            dao.publisher   = newBook.publisher
            dao.year        = newBook.year
            dao.cover       = newBook.cover
            dao.isRead      = newBook.isRead
            dao.updatedAt   = Clock.System.now()
            true
        } else false
    }

    override suspend fun delete(userId: String, bookId: String): Boolean = suspendTransaction {
        BookTable.deleteWhere {
            (BookTable.id eq UUID.fromString(bookId)) and
                    (BookTable.userId eq UUID.fromString(userId))
        } >= 1
    }

    override suspend fun getStats(userId: String): Map<String, Long> = suspendTransaction {
        val userUuid = UUID.fromString(userId)
        val total  = BookDAO.find { BookTable.userId eq userUuid }.count()
        val read   = BookDAO.find { (BookTable.userId eq userUuid) and (BookTable.isRead eq true) }.count()
        val unread = BookDAO.find { (BookTable.userId eq userUuid) and (BookTable.isRead eq false) }.count()
        mapOf("total" to total, "read" to read, "unread" to unread)
    }
}