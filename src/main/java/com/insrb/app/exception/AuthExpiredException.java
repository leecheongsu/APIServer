package com.insrb.app.exception;

public class AuthExpiredException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public AuthExpiredException(String message) {
      super(message);
  }

}