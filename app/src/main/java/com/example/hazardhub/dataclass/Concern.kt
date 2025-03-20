package com.example.hazardhub.dataclass

import com.google.firebase.Timestamp

data class Concern(
    val concernId : String? = "",
    val description : String = "",
    val imageUrl : String = "",
    var status : String ="In Progress",
    val timestamp: Timestamp = Timestamp.now()
)
