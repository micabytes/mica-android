package com.micabytes.math;

public final class Triangular {

  public static int sum(int n) {
    return (n * (n + 1)) / 2;
  }

  @SuppressWarnings({"NumericCastThatLosesPrecision", "MagicNumber"})
  public static int reverse(int n) {
    return (int) Math.floor(0.5 * Math.sqrt((8 * n) + 1) - 0.5);
  }

  private Triangular() {
    throw new AssertionError("Utility classes should never be instantiated");
  }

}
