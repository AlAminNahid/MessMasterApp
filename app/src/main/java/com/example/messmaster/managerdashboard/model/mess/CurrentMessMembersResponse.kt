package com.example.messmaster.managerdashboard.model.mess

data class CurrentMessMembersResponse(
    val messID: Int,
    val messName: String,
    val members: List<MessMember>
)

data class MessMember(
    val member_id: Int,
    val user_id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val role: String
) {
    override fun toString(): String = name
}
