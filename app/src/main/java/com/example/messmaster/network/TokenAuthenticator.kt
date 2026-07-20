package com.example.messmaster.network

import android.util.Log
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

private const val TAG = "TokenAuthenticator"
private const val MAX_RETRIES = 1

// Endpoints that don't require an existing session — a 401 here means the
// submitted credentials were wrong, not that a session expired, so refresh/
// force-logout must not run for them.
private val UNAUTHENTICATED_AUTH_PATHS = listOf(
    "/auth/login",
    "/auth/registration",
    "/auth/forget-password",
)

class TokenAuthenticator(
    private val cookieJar: PersistentCookieJar,
    private val baseUrl: String,
    private val sessionManager: SessionManager,
) : Authenticator {

    private val refreshClient by lazy {
        OkHttpClient.Builder().cookieJar(cookieJar).build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath

        if (UNAUTHENTICATED_AUTH_PATHS.any { path.endsWith(it) }) {
            return null
        }

        if (path.endsWith("/auth/refresh")) {
            sessionManager.forceLogout("Session expired. Please log in again.")
            return null
        }

        if (responseCount(response) > MAX_RETRIES) {
            sessionManager.forceLogout("Session expired. Please log in again.")
            return null
        }

        synchronized(this) {
            // Another thread may have already refreshed the token while we were
            // waiting for the lock; if the cookie looks fresh, just retry.
            if (!cookieJar.hasValidSession()) {
                sessionManager.forceLogout("Session expired. Please log in again.")
                return null
            }

            return try {
                val refreshRequest = Request.Builder()
                    .url("${baseUrl}auth/refresh")
                    .post(ByteArray(0).toRequestBody(null))
                    .build()

                val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                refreshResponse.use {
                    if (it.isSuccessful) {
                        response.request.newBuilder().build()
                    } else {
                        sessionManager.forceLogout("Session expired. Please log in again.")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh failed", e)
                sessionManager.forceLogout("Session expired. Please log in again.")
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
