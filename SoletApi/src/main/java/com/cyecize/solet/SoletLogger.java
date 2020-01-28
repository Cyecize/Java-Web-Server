package com.cyecize.solet;

public interface SoletLogger {

  void info(Object msg, Object... params);

  void warning(Object msg, Object... params);

  void error(Object msg, Object... params);

  void printStackTrace(Throwable throwable);
}
