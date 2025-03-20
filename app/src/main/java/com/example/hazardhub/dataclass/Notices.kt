package com.example.hazardhub.dataclass

import com.google.firebase.Timestamp

data class Notices(
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val projectId: String = ""
)
