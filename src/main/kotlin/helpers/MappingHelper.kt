package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.BookDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Book
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.bio,
    dao.createdAt.toString(),  // convert Instant ke String
    dao.updatedAt.toString(),  // convert Instant ke String
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun bookDAOToModel(dao: BookDAO) = Book(
    id          = dao.id.value.toString(),
    userId      = dao.userId.toString(),
    title       = dao.title,
    author      = dao.author,
    description = dao.description,
    genre       = dao.genre,
    isbn        = dao.isbn,
    publisher   = dao.publisher,
    year        = dao.year,
    cover       = dao.cover,
    isRead      = dao.isRead,
    createdAt   = dao.createdAt.toString(),  // convert Instant ke String
    updatedAt   = dao.updatedAt.toString(),  // convert Instant ke String
)
