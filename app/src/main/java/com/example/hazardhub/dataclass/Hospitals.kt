package com.example.hazardhub.dataclass

data class Hospitals(
    val addressLine1 : String = "",
    val addressLine2 : String = "",
    val contactNumber : String = "",
    val emergencyServices : Boolean = false,
    val endTime : String = "",
    val name : String ="",
    val numberOfBeds : String = "",
    val pincode : String = "",
    val startTime : String = "",
    val state : String = ""
    )
