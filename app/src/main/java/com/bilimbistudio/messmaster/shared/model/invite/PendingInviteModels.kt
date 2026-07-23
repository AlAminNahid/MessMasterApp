package com.bilimbistudio.messmaster.shared.model.invite

data class PendingInvite(val id: Int, val mess_name: String, val invited_by_name: String)

data class PendingInviteResponse(val invite: PendingInvite?)

data class RespondInviteRequest(val inviteId: Int)

data class RespondInviteResponse(val message: String)
