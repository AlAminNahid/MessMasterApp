package com.example.messmaster.auth.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("app_cookies", Context.MODE_PRIVATE)
    private val cookieCache = mutableSetOf<Cookie>()

    init {
        sharedPrefs.all.forEach { (key, value) ->
            if (value is String) {
                Cookie.parse("https://dummy.com".toHttpUrl(), value)?.let {
                    if (it.expiresAt > System.currentTimeMillis()) {
                        cookieCache.add(it)
                    }
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieCache.addAll(cookies)
        val editor = sharedPrefs.edit()
        cookies.forEach { cookie ->
            editor.putString(cookie.name, cookie.toString())
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // Remove expired cookies and return valid ones
        val now = System.currentTimeMillis()
        cookieCache.removeAll { it.expiresAt < now }
        return cookieCache.filter { it.matches(url) }
    }

    fun hasValidSession(): Boolean {
        return cookieCache.any { it.name == "access_token" && it.expiresAt > System.currentTimeMillis() }
    }

    fun clear() {
        cookieCache.clear()
        sharedPrefs.edit().clear().apply()
    }
}
