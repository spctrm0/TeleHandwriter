package main;

import java.util.Calendar;

public class G {
	public static String	grblConnectionChkMsg	= "[MSG:'$H'|'$X' to unlock]";
	public static String	arduinoConnectionChkMsg	= "arduino";

	public static String timestamp() {
		return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", Calendar.getInstance());
	}
}
