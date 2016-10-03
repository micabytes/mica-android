package com.micabytes.math;

public final class Triangular {

  public static int sum(int n) {
    return (n * (n + 1)) / 2;
  }

  @SuppressWarnings({"NumericCastThatLosesPrecision", "MagicNumber"})
  public static int reverse(int n) {
    return (int) Math.floor(0.5 * Math.sqrt((8 * n) + 1) - 0.5);
  }

  public static int threshold(int n) {
    int lev = reverse(n);
    if (lev <= -8) return -3;
    if (lev <= -5) return -2;
    if (lev <= -2) return -1;
    if (lev <= 1) return 0;
    if (lev <= 4) return 1;
    if (lev <= 7) return 2;
    return 3;
  }

  private Triangular() {
    throw new AssertionError("Utility classes should never be instantiated");
  }

}
