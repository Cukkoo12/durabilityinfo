package com.cukkoo.durabilityinfo.core;

public record HudPosition(int x, int y) {
    public static HudPosition clamp(int x, int y, int hudWidth, int hudHeight, int screenWidth, int screenHeight, int margin) {
        int maxX = Math.max(margin, screenWidth - hudWidth - margin);
        int maxY = Math.max(margin, screenHeight - hudHeight - margin);
        return new HudPosition(Math.max(margin, Math.min(maxX, x)), Math.max(margin, Math.min(maxY, y)));
    }

    public static HudPosition fromAnchor(DurabilityInfoConfig.HudAnchor anchor, int offsetX, int offsetY,
                                         int hudWidth, int hudHeight, int screenWidth, int screenHeight) {
        int x = switch (anchor) {
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - hudWidth - offsetX;
            default -> offsetX;
        };
        int y = switch (anchor) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - hudHeight - offsetY;
            default -> offsetY;
        };
        return clamp(x, y, hudWidth, hudHeight, screenWidth, screenHeight, 2);
    }
}
