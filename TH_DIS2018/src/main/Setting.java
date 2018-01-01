package main;

public class Setting {

	public static float	myCalibXInPx			= 0;
	public static float	myCalibYInPx			= 0;
	public static float	targetCalibXInPx	= 0;
	public static float	targetCalibYInPx	= 0;

	public static int			servoHover	= 255 - 32;
	public static int			servoZero		= 127;
	public static float[]	servoDelay	= { 0.025f, 0.150f, 0.025f, 0.150f };

	public static int feedrateStrokeToStoke = 60 * 250; // 1min * (mm / sec)

	public static float xBackOff = -20;

	public static int			myPort			= 8765;
	public static int			targetPort	= 4321;
	public static String	targetIp		= "192.168.0.4";

	public static boolean	isXInverted	= false;
	public static boolean	isYInverted	= true;

	public static float	xZero	= 43.5f;
	public static float	yZero	= 26;

	// public static int myPort = 4321;
	// public static int targetPort = 8765;
	// public static String targetIp = "192.168.0.8";

	// public static boolean isXInverted = true;
	// public static boolean isYInverted = false;

	// public static float xZero = 43;
	// public static float yZero = 31;

	public static String connectionChkTxt = "Grbl 1.1f ['$' for help]";

	public static int	myTabletWidthInMm				= 224;
	public static int	myTabletHeightInMm			= 140;
	public static int	targetTabletWidthInMm		= 224;
	public static int	targetTabletHeightInMm	= 140;

	public static int	myScreenWidthInPx				= 1920;
	public static int	myScreenHeightInPx			= 1080;
	public static int	targetScreentWidthInPx	= 1920;
	public static int	targetScreenHeightInPx	= 1080;
}
