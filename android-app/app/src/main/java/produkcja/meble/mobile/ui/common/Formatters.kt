package produkcja.meble.mobile.ui.common

import java.text.NumberFormat
import java.util.Locale
import produkcja.meble.mobile.BuildConfig

object ServerConfig {
    @Volatile
    var baseUrl: String = BuildConfig.API_BASE_URL
}

private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pl-PL"))

fun money(value: Double): String = currencyFormatter.format(value)

fun number(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

fun date(value: String): String =
    value.substringBefore('T').ifBlank { "Brak terminu" }

fun dateTime(value: String): String =
    value.replace('T', ' ').substringBefore('.').ifBlank { "Brak danych" }

fun String?.isMeaningful(): Boolean {
    if (this == null) return false
    val trimmed = this.trim()
    return trimmed.isNotBlank() &&
        !trimmed.equals("null", ignoreCase = true) &&
        !trimmed.equals("none", ignoreCase = true) &&
        !trimmed.equals("brak", ignoreCase = true) &&
        trimmed != "-"
}

/**
 * Resolves a relative image URL (e.g. "/api/products/images/xyz.jpg")
 * to a fully qualified URL (e.g. "http://10.0.2.2:8080/api/products/images/xyz.jpg")
 * that Coil and other network image loaders can load directly.
 */
fun resolveImageUrl(rawUrl: String?, baseUrl: String = ServerConfig.baseUrl): String? {
    if (!rawUrl.isMeaningful()) return null
    val trimmed = rawUrl!!.trim()
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed
    }
    val cleanBase = baseUrl.trimEnd('/')
    val cleanPath = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
    return "$cleanBase$cleanPath"
}
