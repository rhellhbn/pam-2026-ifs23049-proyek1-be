package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BookTable : UUIDTable("books") {
    val userId     = uuid("user_id")
    val title      = varchar("title", 200)
    val author     = varchar("author", 150)
    val description = text("description")
    val genre      = varchar("genre", 50).default("Umum")
    val isbn       = varchar("isbn", 20).nullable()
    val publisher  = varchar("publisher", 150).nullable()
    val year       = integer("year").nullable()
    val cover      = text("cover").nullable()
    val isRead     = bool("is_read").default(false)
    val createdAt  = timestamp("created_at")
    val updatedAt  = timestamp("updated_at")
}
