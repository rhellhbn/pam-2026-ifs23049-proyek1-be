package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.BookRequest
import org.delcom.data.DataResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IBookRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class BookService(
    private val userRepo: IUserRepository,
    private val bookRepo: IBookRepository
) {

    // ─── GET ALL (dengan pagination, search, filter) ──────────────────────────
    suspend fun getAll(call: ApplicationCall) {
        val user     = ServiceHelper.getAuthUser(call, userRepo)
        val search   = call.request.queryParameters["search"] ?: ""
        val isReadP  = call.request.queryParameters["is_read"]
        val isRead   = when (isReadP) { "1","true" -> true; "0","false" -> false; else -> null }
        val genre    = call.request.queryParameters["genre"]
        val page     = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage  = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        val books = bookRepo.getAll(user.id, search, isRead, genre, page, perPage)
        val total = bookRepo.countAll(user.id, search, isRead, genre)

        call.respond(DataResponse("success", "Berhasil mengambil daftar buku",
            mapOf("books" to books, "total" to total, "page" to page, "perPage" to perPage)))
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────
    suspend fun getById(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val book   = bookRepo.getById(bookId)
        if (book == null || book.userId != user.id) throw AppException(404, "Data buku tidak tersedia!")
        call.respond(DataResponse("success", "Berhasil mengambil data buku", mapOf("book" to book)))
    }

    // ─── POST ─────────────────────────────────────────────────────────────────
    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<BookRequest>()
        request.userId = user.id

        val v = ValidatorHelper(request.toMap())
        v.required("title",       "Judul buku tidak boleh kosong")
        v.required("author",      "Penulis tidak boleh kosong")
        v.required("description", "Deskripsi tidak boleh kosong")
        v.validate()

        val bookId = bookRepo.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan buku", mapOf("bookId" to bookId)))
    }

    // ─── PUT ──────────────────────────────────────────────────────────────────
    suspend fun put(call: ApplicationCall) {
        val bookId  = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<BookRequest>()
        request.userId = user.id

        val v = ValidatorHelper(request.toMap())
        v.required("title",       "Judul buku tidak boleh kosong")
        v.required("author",      "Penulis tidak boleh kosong")
        v.required("description", "Deskripsi tidak boleh kosong")
        v.validate()

        val old = bookRepo.getById(bookId)
        if (old == null || old.userId != user.id) throw AppException(404, "Data buku tidak tersedia!")
        request.cover = old.cover

        val ok = bookRepo.update(user.id, bookId, request.toEntity())
        if (!ok) throw AppException(400, "Gagal memperbarui data buku!")
        call.respond(DataResponse("success", "Berhasil mengubah data buku", null))
    }

    // ─── PUT COVER ────────────────────────────────────────────────────────────
    suspend fun putCover(call: ApplicationCall) {
        val bookId  = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = BookRequest(userId = user.id)

        val multipart = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/books/$fileName"
                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        request.cover = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        if (request.cover == null) throw AppException(404, "Cover buku tidak tersedia!")
        if (!File(request.cover!!).exists()) throw AppException(404, "Cover buku gagal diunggah!")

        val old = bookRepo.getById(bookId)
        if (old == null || old.userId != user.id) throw AppException(404, "Data buku tidak tersedia!")

        request.title       = old.title
        request.author      = old.author
        request.description = old.description
        request.genre       = old.genre
        request.isbn        = old.isbn
        request.publisher   = old.publisher
        request.year        = old.year
        request.isRead      = old.isRead

        val ok = bookRepo.update(user.id, bookId, request.toEntity())
        if (!ok) throw AppException(400, "Gagal memperbarui cover buku!")

        if (old.cover != null) {
            val oldFile = File(old.cover!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse("success", "Berhasil mengubah cover buku", null))
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    suspend fun delete(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val old    = bookRepo.getById(bookId)
        if (old == null || old.userId != user.id) throw AppException(404, "Data buku tidak tersedia!")

        val ok = bookRepo.delete(user.id, bookId)
        if (!ok) throw AppException(400, "Gagal menghapus data buku!")

        old.cover?.let { coverPath ->
            val f = File(coverPath)
            if (f.exists()) f.delete()
        }
        call.respond(DataResponse("success", "Berhasil menghapus buku", null))
    }

    // ─── GET COVER ────────────────────────────────────────────────────────────
    suspend fun getCover(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val book   = bookRepo.getById(bookId) ?: return call.respond(HttpStatusCode.NotFound)
        if (book.cover == null) throw AppException(404, "Buku belum memiliki cover")
        val file = File(book.cover!!)
        if (!file.exists()) throw AppException(404, "Cover buku tidak tersedia")
        call.respondFile(file)
    }

    // ─── GET STATS ────────────────────────────────────────────────────────────
    suspend fun getStats(call: ApplicationCall) {
        val user  = ServiceHelper.getAuthUser(call, userRepo)
        val stats = bookRepo.getStats(user.id)
        call.respond(DataResponse("success", "Berhasil mengambil statistik buku", stats))
    }
}
