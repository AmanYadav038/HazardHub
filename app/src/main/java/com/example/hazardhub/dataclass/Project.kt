package com.example.hazardhub.dataclass

import com.google.firebase.Timestamp
import java.sql.Time

data class Project(
    val projectName: String = "", // Provide default value
    val projectDescription: String? = null, // Optional
    val affectedPeople: String = "", // Provide default value
    val projectAdd1: String = "", // Provide default value
    val projectAdd2: String = "", // Provide default value
    val projectState: String = "", // Provide default value
    val pincode: String = "", // Provide default value
    val initiateTime: Timestamp = Timestamp.now(),
    val initiator: String = "", // Reference to the department's email
    val status: String = "Active", // Default status
    val emergency : Boolean = false,
    val imageUrl: String? = null // Optional
)
