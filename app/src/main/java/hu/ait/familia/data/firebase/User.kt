package hu.ait.familia.data.firebase

import java.util.Date

data class User(
    var userId: String,
    var username: String,
    var email: String,
    var profilePicture: String?,
    var familyIDs: List<String>,
    val createdAt: Date,
    val currentFamily: String? = null
)

data class UserWithId(
    var id: String,
    var user: User
)
