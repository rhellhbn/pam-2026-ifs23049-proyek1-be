package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var username: String,
    var password: String,
    var photo: String? = null,
    var bio: String? = null,
    // Ubah dari Instant ke String
    val createdAt: String = Clock.System.now().toString(),
    var updatedAt: String = Clock.System.now().toString(),
)