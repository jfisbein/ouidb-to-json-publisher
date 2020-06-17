package com.sputnik.ouidb;

public enum ExitCode {
  NO_CHANGES(0), THERES_CHANGES(0), PARAMS_ERROR(-1);

  private final int code;

  ExitCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
