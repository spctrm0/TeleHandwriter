public int		myPort			= 8765;
public int		targetPort	= 4321;
public String	targetIp		= "192.168.0.4";

// public int myPort = 8765;
// public int targetPort = 8765;
// public String targetIp = "192.168.0.8";

public boolean	isXInverted	= false;
public boolean	isYInverted	= true;

public float	xZero	= 43.5f;
public float	yZero	= 26;

//public int    myPort      = 4321;
//public int    targetPort  = 8765;
//public String  targetIp    = "192.168.0.8";

//// public int myPort = 4321;
//// public int targetPort = 4321;
//// public String targetIp = "192.168.0.4";

//public boolean isXInverted = true;
//public boolean isYInverted = false;

//public float xZero = 43;
//public float yZero = 31;

public float	xBackOff	= -20;

public String connectionChkTxt = "Grbl 1.1f ['$' for help]";

public int			servoHover	= 255 - 32;
public int			servoZero		= 127;
public float[]	servoDelay	= { 0.025f, 0.150f, 0.025f, 0.150f };

public int feedrateStrokeToStoke = 60 * 250; // 1min * (mm / sec)

public float	myCalibX			= 0;
public float	myCalibY			= 0;
public float	targetCalibX	= 0;
public float	targetCalibY	= 0;

public int	myTabletWidth				= 224;
public int	myTabletHeight			= 140;
public int	targetTabletWidth		= 224;
public int	targetTabletHeight	= 140;

public int	myScreenWidth				= 1920;
public int	myScreenHeight			= 1080;
public int	targetScreentWidth	= 1920;
public int	targetScreenHeight	= 1080;