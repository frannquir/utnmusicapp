package com.musicspring.app.music_app.model.enums;

import java.util.List;
import java.util.Random;

public enum DefaultAvatar {
    CLASSIC_DOG("classic-dog.png"),
    COUNTRY_HORSE("country-horse.png"),
    HIPHOP_DOG("hiphop-dog.png"),
    LOFI_DOLPHIN("lofi-dolphin.png"),
    METAL_WOLF("metal-wolf.png"),
    OPERA_PANDA("opera-panda.png"),
    POP_CAT("pop-cat.png"),
    POP_DOG("pop-dog.png"),
    REGGAE_DOG("reggae-dog.png"),
    TANGO_BIRD("tango-bird.png"),
    TECHNO_DOG("techno-dog.png"),
    TRAP_BAT("trap-bat.png");
    private final String fileName;

    DefaultAvatar(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
    private static final List<DefaultAvatar> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();
    public static String getRandomAvatarFileName() {
        return VALUES.get(RANDOM.nextInt(SIZE)).getFileName();
    }
}
