package main;

import drawing.Stroke;
import grbl.Grbl;
import grbl.Interpreter;
import grbl.SerialComm;

import java.util.Calendar;

import drawing.Drawing;
import drawing.Point;
import oscComm.OscComm;
import oscP5.OscMessage;
import processing.core.PApplet;
import processing.data.Table;
import processing.serial.Serial;
import tabletInput.TabletInput;

public class TH_DIS2018 extends PApplet {
	Table table;

	KeyInput keyInput;

	TabletInput	tabletInput;
	OscComm			oscComm;
	Drawing			drawing;
	Interpreter	interpreter;
	Grbl				grbl;
	SerialComm	serialComm;

	int acc = 6000;

	public void settings() {
		fullScreen();
	}

	public void setup() {
		table = new Table();
		table.addColumn("strokeIdx");
		table.addColumn("penX");
		table.addColumn("penY");
		table.addColumn("pressure");
		table.addColumn("tiltX");
		table.addColumn("tiltY");
		table.addColumn("millis");

		keyInput = new KeyInput(this);

		tabletInput = new TabletInput(this);
		oscComm = new OscComm(this);
		drawing = new Drawing();
		interpreter = new Interpreter();
		grbl = new Grbl(this);
		serialComm = new SerialComm(this);

		oscComm.setTabletInput(tabletInput);
		oscComm.setDrawing(drawing);

		interpreter.setDrawing(drawing);
		interpreter.setGrbl(grbl);
		interpreter.setTable(table);

		serialComm.setGrbl(grbl);

		grbl.setSerialComm(serialComm);

		thread("OscCommThread");
		thread("InterpreterThread");
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
		grbl.reserve("M3S0\r");
		saveTable(table, "tabletInputLogs\\" + timestamp() + ".csv");
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

	public void keyPressed() {
		if (key == '~') {
			oscComm.tryToConnect();
		}
		else if (key == 'c' || key == 'C') // toggle calibration
		{
			tabletInput.toggleCalibration();
		}
		else if (key == 'i' || key == 'I') // toggle writable
		{
			tabletInput.toggleWritable();
		}
		else if (key == 'h' || key == 'H') // set home
		{
			grbl.reserve("G92X0Y0\r");
			grbl.reserve("G90\r");
		}
		else if (key == '?') // set home
		{
			grbl.reserve("$$\r");
		}
		else if (key == 'b' || key == 'B') // set home
		{
			System.out.print(grbl.bfrSize);
			System.out.print(", ");
			System.out.print(grbl.reservedMsg.size());
			System.out.print(", ");
			System.out.println(grbl.grblBfr.size());
		}
		else if (key == 'x' || key == 'X') // servo off
		{
			grbl.reserve("M3S0\r");
		}
		else if (key == 'w' || key == 'W') // servo up
		{
			grbl.reserve("M3S" + Setting.servoHover + "\r");
		}
		else if (key == 's' || key == 'S') // servo down
		{
			grbl.reserve("M3S" + Setting.servoZero + "\r");
		}
		else if (key == '\'') {
			acc = 4000;
			System.out.println(acc);
		}
		else if (keyCode == UP) {
			acc += 50;
			System.out.println(acc);
		}
		else if (keyCode == DOWN) {
			acc -= 50;
			System.out.println(acc);
		}
		else if (key == '[') {
			grbl.reserve("$120=" + acc + "\r");
		}
		else if (key == ']') {
			grbl.reserve("$121=" + acc + "\r");
		}
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { main.TH_DIS2018.class.getName() };
		if (passedArgs != null) PApplet.main(concat(appletArgs, passedArgs));
		else PApplet.main(appletArgs);
	}

	String timestamp() {
		Calendar now = Calendar.getInstance();
		return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", now);
	}
}
