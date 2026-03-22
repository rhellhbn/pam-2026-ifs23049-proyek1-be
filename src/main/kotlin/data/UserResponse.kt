package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    var id: String = "",
    var name: String = "",
    var username: String = "",
    var photo: String? = null,
    var bio: String? = null,
    var createdAt: String = "",  // String bukan Instant
    var updatedAt: String = "",  // String bukan Instant
)