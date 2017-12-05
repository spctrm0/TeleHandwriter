package main;

import drawing.Stroke;
import grbl.Grbl;
import grbl.Interpreter;
import grbl.SerialComm;
import drawing.Drawing;
import drawing.Point;
import oscComm.OscComm;
import oscP5.OscMessage;
import processing.core.PApplet;
import processing.serial.Serial;
import tabletInput.TabletInput;

public class TH_DIS2018 extends PApplet {
	KeyInput	keyInput;

	TabletInput	tabletInput;	//
	OscComm		oscComm;		//
	Drawing		drawing;		//
	Interpreter	interpreter;	//
	Grbl		grbl;			//
	SerialComm	serialComm;		//

	public void settings() {
		fullScreen(1);
	}

	public void setup() {
		keyInput = new KeyInput(this);

		tabletInput = new TabletInput(this);
		oscComm = new OscComm(this);
		drawing = new Drawing();
		interpreter = new Interpreter();
		grbl = new Grbl();
		serialComm = new SerialComm(this);

		oscComm.setTabletInput(tabletInput);
		oscComm.setDrawing(drawing);

		interpreter.setDrawing(drawing);
		interpreter.setGrbl(grbl);

		serialComm.setGrbl(grbl);

		grbl.setSerialComm(serialComm);

		thread("OscCommThread");
		thread("InterpreterThread");
		thread("SerialCommThread");
		thread("GrblThread");
	}

	public void draw() {
		background(serialComm.isConnected ? 0 : 255, oscComm.isConnected ? 0 : 255,
				(!tabletInput.modeCalibration && tabletInput.modeWritable) ? 0 : 255);

		noFill();
		stroke(255);
		for (int i = 0; i < drawing.strokes.size(); i++) {
			Stroke stroke_ = drawing.strokes.get(i);
			for (int j = 0; j < stroke_.points.size() - 1; j++) {
				Point a_ = stroke_.points.get(j);
				Point b_ = stroke_.points.get(j + 1);
				line(a_.penX, a_.penY, b_.penX, b_.penY);
			}
		}
	}

	public void exit() {
		println("a");
		super.exit();
	}

	public void oscEvent(OscMessage _oscMsg) {
		oscComm.receive(_oscMsg);
	}

	public void serialEvent(Serial _serialEvt) {
		char replyChar_ = _serialEvt.readChar();
		serialComm.read(replyChar_);
	}

	public void OscCommThread() {
		while (true) {
			oscComm.thread();
		}
	}

	public void InterpreterThread() {
		while (true) {
			interpreter.thread();
		}
	}

	public void GrblThread() {
		while (true) {
			grbl.thread();
		}
	}

	public void SerialCommThread() {
		while (!serialComm.isConnected) {
			serialComm.thread();
		}
	}

	public void keyPressed() {
		if (key == '~') {
			oscComm.tryToConnect();
		} else if (key == 'c' || key == 'C') // toggle calibration
		{
			tabletInput.toggleCalibration();
		} else if (key == 'i' || key == 'I') // toggle writable
		{
			tabletInput.toggleWritable();
		} else if (key == 'h' || key == 'H') // set home
		{
			grbl.reserve("G92X0Y0\r");
			grbl.reserve("G90\r");
			grbl.reserve("G93\r");
		} else if (key == 'q' || key == 'Q') // servo off
		{
			grbl.reserve("M3S0\r");
		} else if (key == 'a' || key == 'A') // servo up
		{
			grbl.reserve("M3S" + Setting.servoHover + "\r");
		} else if (key == 'z' || key == 'Z') // servo down
		{
			grbl.reserve("M3S" + Setting.servoZero + "\r");
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
