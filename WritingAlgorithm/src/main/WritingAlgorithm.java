package main;

import java.util.ArrayList;
import java.util.Calendar;

import main.TabletInput.ReceivingPoint;
import processing.core.PApplet;
import processing.core.PGraphics;

public class WritingAlgorithm extends PApplet {
	TabletInput								tabletInput;
	ArrayList<ReceivingPoint>	writingMethods	= new ArrayList<ReceivingPoint>();
	ArrayList<Drawing>				drawings				= new ArrayList<Drawing>();
	boolean										overlayMode			= true;

	public void settings() {
		fullScreen(2);
	}

	public void setup() {
		tabletInput = new TabletInput(this, (int) (width / 4.0f), height);
		writingMethods.add(new MassMethod(this, (int) (width / 4.0f), height));
		writingMethods.add(new MassMethod(this, (int) (width / 4.0f), height));
		writingMethods.add(new MassMethod(this, (int) (width / 4.0f), height));

		drawings.add(tabletInput);
		for (ReceivingPoint writingMethod_ : writingMethods)
			drawings.add((Drawing) writingMethod_);

		MassMethod m;
		m = (MassMethod) writingMethods.get(0);
		m.setMass(2);
		m.setDamping(0.5f);
		m = (MassMethod) writingMethods.get(1);
		m.setMass(4);
		m.setDamping(0.75f);
		m = (MassMethod) writingMethods.get(2);
		m.setMass(10);
		m.setDamping(0.92f);

		for (ReceivingPoint writingMethod_ : writingMethods)
			tabletInput.addReceivingPointObj(writingMethod_);
	}

	public void draw() {
		background(255);
		image(tabletInput.getPGraphics(), 0, 0);
		if (overlayMode)
			tabletInput.getPGraphics().tint(255, 32);
		for (int i = 0; i < writingMethods.size(); i++) {
			image(((Drawing) writingMethods.get(i)).getPGraphics(), width / (writingMethods.size() + 1) * (i + 1), 0);
			if (overlayMode)
				image(tabletInput.getPGraphics(), width / (writingMethods.size() + 1) * (i + 1), 0);
		}
		if (overlayMode)
			tabletInput.getPGraphics().noTint();

		noFill();
		stroke(0);
		for (int i = 0; i < writingMethods.size(); i++)
			line(width / (writingMethods.size() + 1) * (i + 1), 0, width / (writingMethods.size() + 1) * (i + 1), height);

		stroke(0, 255, 0);
		line(pmouseX, pmouseY, mouseX, mouseY);
	}

	public void keyPressed() {
		if (key == 'C' || key == 'c') {
			for (Drawing drawing_ : drawings)
				drawing_.getPGraphics().clear();
		}
		else if (key == 'O' || key == 'o') {
			overlayMode = !overlayMode;
		}
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