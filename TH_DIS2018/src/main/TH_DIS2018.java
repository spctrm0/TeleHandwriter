package main;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.serial.Serial;

public class TH_DIS2018 extends PApplet {
	KeyInput	keyInput;
	TabletInput	tabletInput;
	OscComm		oscComm;
	Strokes		strokes;
	Interpreter	interpreter;
	SerialComm	serialComm;
	Grbl		grbl;

	public void settings() {
		fullScreen(1);
	}

	public void setup() {
		keyInput = new KeyInput(this);
		tabletInput = new TabletInput(this);
		oscComm = new OscComm(this);
		strokes = new Strokes();
		interpreter = new Interpreter();
		serialComm = new SerialComm(this);
		grbl = new Grbl();

		tabletInput.setOscComm(oscComm);
		oscComm.setStrokes(strokes);
		interpreter.setStrokes(strokes);
		interpreter.setGrbl(grbl);
		serialComm.setGrbl(grbl);

		textAlign(CENTER);

		thread("InterpreterThread");
		thread("SerialCommThread");
	}

	public void draw() {
		background(serialComm.isConnected ? 0 : 255, oscComm.isConnected ? 0 : 255,
				(!tabletInput.isCalibrationMode && tabletInput.isWritable) ? 0 : 255);

		noStroke();
		fill(255);
		text(keyInput.charToStrBfr.toString(), width / 2.0f, height / 2.0f);

		noFill();
		stroke(255);
		for (int i = 0; i < strokes.strokes.size(); i++) {
			Points stroke_ = strokes.strokes.get(i);
			for (int j = 0; j < stroke_.points.size() - 1; j++) {
				Point a_ = stroke_.points.get(j);
				Point b_ = stroke_.points.get(j + 1);
				line(a_.penX, a_.penY, b_.penX, b_.penY);
			}
		}
		// System.out.println(strokes.strokes.size() + ", " +
		// strokes.strokes.get(strokes.strokes.size() - 1).points.size());
	}

	public void serialEvent(Serial _serialEvt) {
		char replyChar_ = _serialEvt.readChar();
		serialComm.read(replyChar_);
	}

	public void oscEvent(OscMessage _oscMsg) {
		oscComm.receive(_oscMsg);
	}

	public void InterpreterThread() {
		while (true) {
			interpreter.pre();
		}
	}

	public void SerialCommThread() {
		while (!serialComm.isConnected) {
			serialComm.pre();
		}
	}

	public void keyPressed() {
		if (key == '~') {
			oscComm.tryConnect();
		} else if (key == 'c' || key == 'C') {
			tabletInput.toggleCalibrationMode();
		} else if (key == 'i' || key == 'I') {
			tabletInput.toggleWritable();
		}
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { main.TH_DIS2018.class.getName() };
		if (passedArgs != null)
			PApplet.main(concat(appletArgs, passedArgs));
		else
			PApplet.main(appletArgs);
	}
}
