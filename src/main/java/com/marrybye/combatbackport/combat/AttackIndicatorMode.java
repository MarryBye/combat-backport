package com.marrybye.combatbackport.combat;

public enum AttackIndicatorMode {

    DISABLED("Disabled"),
    CROSSHAIR("Crosshair"),
    HOTBAR("Hotbar");

    private final String displayName;

    AttackIndicatorMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AttackIndicatorMode fromString(String name) {
        for (AttackIndicatorMode mode : values()) {
            if (mode.name()
                .equalsIgnoreCase(name) || mode.displayName.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return CROSSHAIR;
    }
}
