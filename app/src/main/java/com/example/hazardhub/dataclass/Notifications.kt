package com.example.hazardhub.dataclass

import com.google.firebase.Timestamp

data class Notifications(
    val notificationId: String = "",
    val type: String = "",
    val projectId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    var read: Boolean = false
)
