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
	Grbl				grbl;
	SerialComm	serialComm;

	Drawing			drawing;		//
	OscComm			oscComm;		//
	TabletInput	tabletInput;//

	Table				table;
	Interpreter	interpreter;

	int acc = 6000;

	public void settings() {
		// fullScreen();
		size(500, 500);
	}

	public void setup() {
		grbl = new Grbl(this);
		serialComm = new SerialComm(this);

		drawing = new Drawing();
		oscComm = new OscComm(this);
		tabletInput = new TabletInput(this);

		table = new Table();
		table.addColumn("strokeIdx");
		table.addColumn("penX");
		table.addColumn("penY");
		table.addColumn("pressure");
		table.addColumn("tiltX");
		table.addColumn("tiltY");
		table.addColumn("millis");
		interpreter = new Interpreter(this);

		grbl.setSerialComm(serialComm);
		serialComm.setGrbl(grbl);

		oscComm.setDrawing(drawing);
		tabletInput.setOscComm(oscComm);

		interpreter.setDrawing(drawing);
		interpreter.setGrbl(grbl);
		interpreter.setTable(table);
	}

	public void draw() {
		background(serialComm.isConnected ? 0 : 255, oscComm.isConnected ? 0 : 255, (tabletInput.isWritable()) ? 0 : 255);
		noFill();
		stroke(255);
		for (Stroke stroke_ : drawing.getStrokes()) {
			for (int j = 0; j < stroke_.getPointsNum() - 1; j++) {
				Point a_ = stroke_.getPoints().get(j);
				Point b_ = stroke_.getPoints().get(j + 1);
				line(a_.getPenX(), a_.getPenY(), b_.getPenX(), b_.getPenY());
			}
		}
		// System.out.print(grbl.bfrSize);
		// System.out.print(", ");
		// System.out.print(grbl.grblBfr.size());
		// System.out.print(", ");
		// System.out.print(grbl.reservedMsg.size());
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

	public void keyPressed() {
		if (key == '~') {
			oscComm.activateAutoConnect();
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
