package com.bintianqi.owndroid.feature.users

class UserInformation(
    val multiUser: Boolean = false,
    val headless: Boolean = false,
    val system: Boolean = false,
    val admin: Boolean = false,
    val demo: Boolean = false,
    val time: Long = 0,
    val logout: Boolean = false,
    val ephemeral: Boolean = false,
    val affiliated: Boolean = false,
    val serial: Long = 0
)

class UserIdentifier(val id: Int, val serial: Long)

enum class UserOperationType {
    Start, Switch, Stop, Delete
}

class CreateUserResult(val message: Int, val serial: Long = -1)
