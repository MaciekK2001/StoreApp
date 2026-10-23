package produkcja.meble.mobile.data

enum class FurnitureType(val displayName: String) {
    CHAIR("Krzesło"),
    ARMCHAIR("Fotel"),
    SOFA("Sofa"),
    CORNER_SOFA("Narożnik"),
    TABLE("Stół"),
    COFFEE_TABLE("Stolik kawowy"),
    DESK("Biurko"),
    BED("Łóżko"),
    WARDROBE("Szafa"),
    BOOKCASE("Regał"),
    COMMODE("Komoda"),
    NIGHTSTAND("Szafka nocna"),
    SHELF("Półka");

    companion object {
        fun from(value: String?): FurnitureType? {
            if (value.isNullOrBlank()) return null
            val trimmed = value.trim()
            entries.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true) ||
                    it.displayName.equals(trimmed, ignoreCase = true)
            }?.let { return it }

            return entries
                .sortedByDescending { it.displayName.length }
                .firstOrNull {
                    trimmed.contains(it.displayName, ignoreCase = true) ||
                        trimmed.contains(it.name, ignoreCase = true)
                }
        }

        fun displayLabel(value: String): String {
            val exactEnum = entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
            return exactEnum?.displayName ?: value
        }
    }
}
