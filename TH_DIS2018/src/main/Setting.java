package main;

public class Setting {
	public static String	connectionChkTxt	= "Grbl 1.1f ['$' for help]";

//	public static int		myPort				= 8765;
//	public static int		targetPort			= 5432;
//	public static String	targetIp			= "192.168.0.5";
	
	public static int		myPort				= 8765;
	public static int		targetPort			= 8765;
	public static String	targetIp			= "192.168.0.8";

	public static boolean	isXInverted			= true;
	public static boolean	isYInverted			= false;

	public static int		servoHover			= 255;
	public static int		servoZero			= 1;

	public static int		feedRateDefault		= 30000;

	public static float		myCalibX			= 0;
	public static float		myCalibY			= 0;
	public static float		targetCalibX		= 0;
	public static float		targetCalibY		= 0;

	public static int		myTabletWidth		= 224;
	public static int		myTabletHeight		= 140;
	public static int		targetTabletWidth	= 224;
	public static int		targetTabletHeight	= 140;

	public static int		myScreenWidth		= 1920;
	public static int		myScreenHeight		= 1080;
	public static int		targetScreentWidth	= 1920;
	public static int		targetScreenHeight	= 1080;
}
