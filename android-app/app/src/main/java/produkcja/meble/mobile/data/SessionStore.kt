package produkcja.meble.mobile.data

import android.content.Context
import org.json.JSONObject
import produkcja.meble.mobile.BuildConfig

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("meble_session", Context.MODE_PRIVATE)

    fun getServerUrl(): String {
        return preferences.getString("server_url", BuildConfig.API_BASE_URL) ?: BuildConfig.API_BASE_URL
    }

    fun setServerUrl(url: String) {
        val trimmed = url.trim()
        val sanitized = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        preferences.edit().putString("server_url", sanitized).apply()
    }

    fun resetServerUrl() {
        preferences.edit().remove("server_url").apply()
    }

    fun load(): AuthSession? {
        val token = preferences.getString("token", null) ?: return null
        val tokenType = preferences.getString("token_type", "Bearer") ?: "Bearer"
        val userJson = preferences.getString("user", null) ?: return null
        return runCatching {
            val json = JSONObject(userJson)
            AuthSession(
                tokenType = tokenType,
                token = token,
                user = User(
                    id = json.getLong("id"),
                    username = json.getString("username"),
                    email = json.getString("email"),
                    role = json.getString("role"),
                ),
            )
        }.getOrNull()
    }

    fun save(session: AuthSession) {
        val user = JSONObject()
            .put("id", session.user.id)
            .put("username", session.user.username)
            .put("email", session.user.email)
            .put("role", session.user.role)
        preferences.edit()
            .putString("token_type", session.tokenType)
            .putString("token", session.token)
            .putString("user", user.toString())
            .apply()
    }

    fun clear() {
        preferences.edit()
            .remove("token_type")
            .remove("token")
            .remove("user")
            .apply()
    }
}
