package main;

public class Setting {
	static String	connectionChkTxt	= "Grbl 1.1f ['$' for help]";

	static int		myPort				= 6666;
	static int		targetPort			= 6666;
	static String	targetIp			= "192.168.0.2";

	static boolean	isXInverted			= true;
	static boolean	isYInverted			= false;

	static int		servoHover			= 96;
	static int		servoZero			= 63;

	static int		feedRateDefault		= 120; // 60000 / 500

	static float	myCalibX			= 0;
	static float	myCalibY			= 0;
	static float	targetCalibX		= 0;
	static float	targetCalibY		= 0;

	static int		myTabletWidth		= 224;
	static int		myTabletHeight		= 140;
	static int		targetTabletWidth	= 224;
	static int		targetTabletHeight	= 140;

	static int		myScreenWidth		= 1920;
	static int		myScreenHeight		= 1080;
	static int		targetScreentWidth	= 1920;
	static int		targetScreenHeight	= 1080;
}
