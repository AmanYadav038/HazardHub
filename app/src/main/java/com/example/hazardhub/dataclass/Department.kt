package com.example.hazardhub.dataclass

data class Department(
    val name: String = "",
    val email : String = "",
    val stateOfOperation:ArrayList<String> = arrayListOf()
)
