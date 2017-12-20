package main;

public class Setting {
	public static void update() {
		xZero = 43.5f;
		yZero = 26;

		servoHover = 255 - 32;
		servoZero = 127;

		servoDelay[0] = 0;
		servoDelay[1] = 0.001f;
		servoDelay[2] = 0;
		servoDelay[3] = 0.001f;
	}

	public static int			myPort			= 8765;
	public static int			targetPort	= 4321;
	public static String	targetIp		= "192.168.0.4";

	// public static int myPort = 8765;
	// public static int targetPort = 8765;
	// public static String targetIp = "192.168.0.8";

	public static boolean	isXInverted	= false;
	public static boolean	isYInverted	= true;

	public static float	xZero	= 43.5f;
	public static float	yZero	= 26;

	// public static int myPort = 4321;
	// public static int targetPort = 8765;
	// public static String targetIp = "192.168.0.8";
	//
	// // public static int myPort = 4321;
	// // public static int targetPort = 4321;
	// // public static String targetIp = "192.168.0.4";
	//
	// public static boolean isXInverted = true;
	// public static boolean isYInverted = false;
	//
	// public static float xZero = 43;
	// public static float yZero = 31;

	public static String connectionChkTxt = "Grbl 1.1f ['$' for help]";

	public static int			servoHover	= 255 - 32;
	public static int			servoZero		= 127;
	public static float[]	servoDelay	= { 0.001f, 0.001f, 0.001f, 0.001f };

	public static int feedrateStrokeToStoke = 60 * 250; // 1min * (mm / sec)

	public static float	myCalibX			= 0;
	public static float	myCalibY			= 0;
	public static float	targetCalibX	= 0;
	public static float	targetCalibY	= 0;

	public static int	myTabletWidth				= 224;
	public static int	myTabletHeight			= 140;
	public static int	targetTabletWidth		= 224;
	public static int	targetTabletHeight	= 140;

	public static int	myScreenWidth				= 1920;
	public static int	myScreenHeight			= 1080;
	public static int	targetScreentWidth	= 1920;
	public static int	targetScreenHeight	= 1080;
}
