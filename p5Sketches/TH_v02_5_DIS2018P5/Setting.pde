static class Setting {
  static float myCalibXInPx = 0;
  static float myCalibYInPx = 0;
  static float targetCalibXInPx = 0;
  static float targetCalibYInPx = 0;

  static int servoHover = 255 - 32;
  static int servoZero = 127;
  static float servoDelay0 = 0.025f;
  static float servoDelay1 = 0.150f;
  static float servoDelay2 = 0.025f;
  static float servoDelay3 = 0.150f;

  static float feedrateStrokeToStoke = 60 * 250; // 1min * (mm / sec)

  static boolean isXInverted = false;
  static boolean isYInverted = true;

  static float xZero = 43.5f;
  static float yZero = 26;
  static float xBack = -20;

  static int myPort = 8765;
  static int targetPort = 4321;
  static String targetIp = "192.168.0.4";

  // static int myPort = 4321;
  // static int targetPort = 8765;
  // static String targetIp = "192.168.0.8";

  // static boolean isXInverted = true;
  // static boolean isYInverted = false;

  // static float xZero = 43;
  // static float yZero = 31;

  static String connectionChkTxt = "[MSG:'$H'|'$X' to unlock]";

  static int myTabletWidthInMm = 224;
  static int myTabletHeightInMm = 140;
  static int targetTabletWidthInMm = 224;
  static int targetTabletHeightInMm = 140;

  static int myScreenWidthInPx = 1920;
  static int myScreenHeightInPx = 1080;
  static int targetScreentWidthInPx = 1920;
  static int targetScreenHeightInPx = 1080;
}