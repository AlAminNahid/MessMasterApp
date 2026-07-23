package com.bilimbistudio.messmaster.managerdashboard.model.mess

data class MessPasswordResponse(val mess_password: String)
data class MessPasswordUpdateResponse(val message: String)
data class ChangeMessPasswordRequest(val accountPassword: String, val newMessPassword: String)
data class ViewMessPasswordRequest(val accountPassword: String)
