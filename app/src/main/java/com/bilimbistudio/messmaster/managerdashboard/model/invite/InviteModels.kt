package com.bilimbistudio.messmaster.managerdashboard.model.invite

data class SearchUserResponse(val id: Int, val name: String, val email: String)

data class InviteMemberRequest(val email: String)

data class InviteMemberResponse(val message: String)
