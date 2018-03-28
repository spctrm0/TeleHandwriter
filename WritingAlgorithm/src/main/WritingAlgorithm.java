package main;

import java.util.Calendar;

import processing.core.PApplet;

public class WritingAlgorithm extends PApplet {

	public void settings() {
		fullScreen(1);
	}

	public void setup() {

	}

	public void draw() {

	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { main.WritingAlgorithm.class.getName() };
		if (passedArgs != null)
			PApplet.main(concat(appletArgs, passedArgs));
		else
			PApplet.main(appletArgs);
	}

	String timestamp() {
		Calendar now = Calendar.getInstance();
		return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", now);
	}
}