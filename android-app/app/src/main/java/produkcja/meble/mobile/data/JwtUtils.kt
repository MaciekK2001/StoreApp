package produkcja.meble.mobile.data

import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Base64

object JwtUtils {

    /**
     * Sprawdza, czy token JWT jest wygasły lub czy wygaśnie w ciągu [marginSeconds] sekund.
     * W przypadku pustego lub niepoprawnego tokenu zwraca true (uznaje za wygasły).
     */
    fun isExpired(token: String?, marginSeconds: Long = 60): Boolean {
        if (token.isNullOrBlank()) return true
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true
            val payload = parts[1]
            val decodedBytes = try {
                Base64.getUrlDecoder().decode(payload)
            } catch (_: Exception) {
                Base64.getDecoder().decode(payload)
            }
            val json = JSONObject(String(decodedBytes, StandardCharsets.UTF_8))
            if (!json.has("exp")) return false
            val expSeconds = json.getLong("exp")
            val currentSeconds = System.currentTimeMillis() / 1000
            currentSeconds + marginSeconds >= expSeconds
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Zwraca czas wygaśnięcia tokenu w milisekundach (epoch millis) lub null, jeśli nie udało się odczytać.
     */
    fun getExpirationMillis(token: String?): Long? {
        if (token.isNullOrBlank()) return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]
            val decodedBytes = try {
                Base64.getUrlDecoder().decode(payload)
            } catch (_: Exception) {
                Base64.getDecoder().decode(payload)
            }
            val json = JSONObject(String(decodedBytes, StandardCharsets.UTF_8))
            if (json.has("exp")) json.getLong("exp") * 1000L else null
        } catch (_: Exception) {
            null
        }
    }
}
