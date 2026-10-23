package produkcja.meble.mobile.data

import android.content.Context
import org.json.JSONObject

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("meble_session", Context.MODE_PRIVATE)

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
        preferences.edit().clear().apply()
    }
}
