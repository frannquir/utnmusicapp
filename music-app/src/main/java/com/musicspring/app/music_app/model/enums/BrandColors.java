package com.musicspring.app.music_app.model.enums;

public enum BrandColors {
    PRIMARY("#af642d"),
    BACKGROUND_MAIN("#faf0e6"),
    BACKGROUND_CARD("#fdf6e3"),
    TEXT_MAIN("#4e342e"),
    TEXT_SECONDARY("#a1887f"),
    TEXT_ON_PRIMARY("#faf0e6");

    private final String hexCode;

    BrandColors(String hexCode) {
        this.hexCode = hexCode;
    }
    public String getHex() {
        return hexCode;
    }
    @Override
    public String toString() {
        return hexCode;
    }
}
