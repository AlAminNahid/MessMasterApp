package com.bilimbistudio.messmaster.network

import okhttp3.Interceptor
import okhttp3.Response

class RemovedMemberInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 403) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            if (body.contains("no longer an active member", ignoreCase = true)) {
                sessionManager.forceLogout("You have been removed from this mess by the manager.")
            }
        }
        return response
    }
}
