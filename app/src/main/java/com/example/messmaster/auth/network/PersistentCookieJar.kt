package com.example.messmaster.auth.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class PersistentCookieJar(context: Context) : CookieJar {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("app_cookies", Context.MODE_PRIVATE)

    private val cookieCache = mutableMapOf<String, Cookie>()

    init {
        sharedPrefs.all.forEach { (key, value) ->
            if (value is String && key.contains("|")) {
                val host = key.substringBefore("|")
                val scheme = if (host.contains("localhost") || host.startsWith("10.")) "http" else "https"
                val url = "$scheme://$host".toHttpUrlOrNull()
                
                if (url != null) {
                    Cookie.parse(url, value)?.let {
                        if (it.expiresAt > System.currentTimeMillis()) {
                            cookieCache[key] = it
                        }
                    }
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = sharedPrefs.edit()
        cookies.forEach { cookie ->
            val key = "${url.host}|${cookie.name}"
            cookieCache[key] = cookie
            editor.putString(key, cookie.toString())
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val iterator = cookieCache.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.expiresAt < now) {
                iterator.remove()
            }
        }
        
        return cookieCache.values.filter { it.matches(url) }
    }

    fun hasValidSession(): Boolean {
        return cookieCache.values.any { it.name == "access_token" && it.expiresAt > System.currentTimeMillis() }
    }

    fun clear() {
        cookieCache.clear()
        sharedPrefs.edit().clear().apply()
    }
}
