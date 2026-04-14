package io.unityfoundation.auth.exceptions;

public class UserDisabledException extends RuntimeException {

  public UserDisabledException(String message) {
    super(message);
  }
}
