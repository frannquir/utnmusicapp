package com.musicspring.app.music_app.exception;

public class AccountDeactivatedException extends RuntimeException {
  private final Long userId;

  public AccountDeactivatedException(String msg, Long userId) {
    super(msg);
    this.userId = userId;
  }

  public Long getUserId() {
    return userId;
  }
}
