package hu.ait.familia.data.firebase

import java.util.Date

data class Family(
    var familyId: String,
    var familyName: String,
    var members: List<String>,
    var createdAt: Date
)