package com.musicspring.app.music_app.exception;

public class AccountBannedException extends RuntimeException {
    public AccountBannedException(String msg) {
        super(msg);
    }
}