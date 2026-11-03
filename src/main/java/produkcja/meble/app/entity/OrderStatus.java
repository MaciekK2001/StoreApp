package produkcja.meble.app.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    ACTIVE("Aktywne"),
    COMPLETED("Zrealizowane"),
    CANCELLED("Anulowane");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static OrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        String normalized = value.trim().toUpperCase();
        for (OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        if (normalized.startsWith("ZREALIZOWAN")) {
            return COMPLETED;
        }
        if (normalized.startsWith("ANULOWAN")) {
            return CANCELLED;
        }
        if (normalized.startsWith("AKTYWN")) {
            return ACTIVE;
        }
        return ACTIVE;
    }
}
