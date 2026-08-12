package com.bagomri.fajrloop.ui.main

data class LoopMemberItem(
    val userId: String,
    val displayName: String,
    val photoUrl: String,
    val status: String,
    val isCurrentUser: Boolean,
    val role: String = "member",
    val responsibleForUserId: String = "",
    val targetName: String = "",
    val position: Int = 1
)

data class FriendWakeAlert(
    val uid: String,
    val displayName: String,
    val message: String
)
