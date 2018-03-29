package main;

import java.util.ArrayList;
import java.util.Calendar;

import main.TabletInput.ReceivingPoint;
import processing.core.PApplet;

public class WritingAlgorithm extends PApplet {
	TabletInput								tabletInput;
	ArrayList<ReceivingPoint>	writingMethods	= new ArrayList<ReceivingPoint>();

	public void settings() {
		fullScreen(1);
	}

	public void setup() {
		tabletInput = new TabletInput(this);
		writingMethods.add(new MassMethod(this));
		writingMethods.add(new MovingAverageMethod(this));
		for (ReceivingPoint writingMethod_ : writingMethods)
			tabletInput.addReceivingPointObj(writingMethod_);
	}

	public void draw() {
		background(255);
		image(tabletInput.getPGraphics(), 0, 0);
		image(((Drawing) writingMethods.get(1)).getPGraphics(), 0, 0);
		noFill();
		stroke(0, 255, 0);
		line(pmouseX, pmouseY, mouseX, mouseY);
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