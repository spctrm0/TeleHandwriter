package main;

import drawing.Stroke;
import grbl.Grbl;
import grbl.Interpreter;
import grbl.SerialComm;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import drawing.Drawing;
import drawing.Point;
import oscComm.OscComm;
import oscP5.OscMessage;
import processing.core.PApplet;
import processing.data.Table;
import processing.serial.Serial;
import tabletInput.TabletInput;

public class TH_DIS2018 extends PApplet {
	Drawing			drawing;
	OscComm			oscComm;
	TabletInput	tabletInput;

	Grbl				grbl;
	SerialComm	serialComm;

	Table				table;
	Interpreter	interpreter;

	StringBuffer strBfr = null;

	long		homeCmdTriggeredUsec	= 0;
	int			waitingTimeMsec				= 5000;
	int			waitingTimeMsecSet		= 5000;
	boolean	isHomeCmdExecuted			= false;

	public void settings() {
		fullScreen();
		// size(500, 500);
	}

	public void homeCmd() {
		strBfr.append("M3")//
				.append("S").append(Setting.servoHover)//
				.append('\r');
		grbl.reserve(strBfr.toString());
		strBfr.setLength(0);

		grbl.reserve("G92X0Y0\r");
		grbl.reserve("G90\r");
		grbl.reserve("G94\r");
		strBfr.append("G1")//
				.append("F").append(Setting.feedrateStrokeToStoke)//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xZero : Setting.xZero))//
				.append("Y").append(String.format("%.3f", Setting.isYInverted ? -Setting.yZero : Setting.yZero))//
				.append('\r');
		grbl.reserve(strBfr.toString());
		strBfr.setLength(0);
		grbl.reserve("G93\r");
		grbl.reserve("G92X0Y0\r");
		isHomeCmdExecuted = true;
	}

	public void homeCmdTrigger() {
		homeCmdTriggeredUsec = System.nanoTime();
		isHomeCmdExecuted = false;
	}

	public long getWaitingTimeMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - homeCmdTriggeredUsec);
	}

	public void setWritable(boolean _isWritable) {
		tabletInput.setWritable(_isWritable);
	}

	public void loadSetting() {
		String[] lines_ = loadStrings("Setting.txt");
		for (String line_ : lines_) {
			String[] parsed_ = line_.split("=");
			if (parsed_.length >= 2) {
				parsed_[0] = parsed_[0].trim();
				parsed_[1] = parsed_[1].trim();
				if (parsed_[0].equals("myPort"))
					Setting.myPort = Integer.parseInt(parsed_[1]);
				else if (parsed_[0].equals("targetPort"))
					Setting.targetPort = Integer.parseInt(parsed_[1]);
				else if (parsed_[0].equals("targetIp"))
					Setting.targetIp = parsed_[1].toString();
				else if (parsed_[0].equals("isXInverted"))
					Setting.isXInverted = Boolean.parseBoolean(parsed_[1]);
				else if (parsed_[0].equals("isYInverted"))
					Setting.isYInverted = Boolean.parseBoolean(parsed_[1]);
				else if (parsed_[0].equals("xZero"))
					Setting.xZero = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("yZero"))
					Setting.yZero = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoHover"))
					Setting.servoHover = Integer.parseInt(parsed_[1]);
				else if (parsed_[0].equals("servoZero"))
					Setting.servoZero = Integer.parseInt(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[0]"))
					Setting.servoDelay[0] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[1]"))
					Setting.servoDelay[1] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[2]"))
					Setting.servoDelay[2] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[3]"))
					Setting.servoDelay[3] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("targetCalibX"))
					Setting.targetCalibX = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("targetCalibY"))
					Setting.targetCalibY = Float.parseFloat(parsed_[1]);
			}
		}
	}

	public void setup() {
		loadSetting();

		drawing = new Drawing();
		oscComm = new OscComm(this);
		tabletInput = new TabletInput(this);

		grbl = new Grbl(this);
		serialComm = new SerialComm(this);

		table = new Table();
		table.addColumn("totalPointIdx");
		table.addColumn("strokeIdx");
		table.addColumn("pointIdx");
		table.addColumn("penX");
		table.addColumn("penY");
		table.addColumn("x");
		table.addColumn("y");
		table.addColumn("f");
		table.addColumn("pressure");
		table.addColumn("tiltX");
		table.addColumn("tiltY");
		table.addColumn("millis");
		table.addColumn("kind");
		interpreter = new Interpreter(this);

		strBfr = new StringBuffer();

		oscComm.setDrawing(drawing);
		tabletInput.setOscComm(oscComm);

		grbl.setSerialComm(serialComm);
		grbl.setOscComm(oscComm);
		serialComm.setGrbl(grbl);

		interpreter.setDrawing(drawing);
		interpreter.setGrbl(grbl);
		interpreter.setTable(table);

		homeCmdTrigger();
		waitingTimeMsec = waitingTimeMsecSet;
	}

	public void draw() {
		if (!isHomeCmdExecuted) {
			if (getWaitingTimeMsec() >= waitingTimeMsec) {
				homeCmd();
				setWritable(true);
				waitingTimeMsec = waitingTimeMsecSet;
			}
		}

		background(serialComm.isConnected ? 0 : 255, oscComm.isConnected ? 0 : 255, (tabletInput.isWritable()) ? 0 : 255);
		noStroke();
		fill(oscComm.isTargetIdle ? 0 : 255, oscComm.isTargetIdle ? 255 : 0, 0);
		rect(0, 0, 32, 32);
		noFill();
		stroke(255);
		for (Stroke stroke_ : drawing.getStrokes()) {
			for (int j = 0; j < stroke_.getPointsNum() - 1; j++) {
				Point a_ = stroke_.getPoints().get(j);
				Point b_ = stroke_.getPoints().get(j + 1);
				line(a_.getX(), a_.getY(), b_.getX(), b_.getY());
			}
		}
		// System.out.print(grbl.bfrSize);
		// System.out.print(", ");
		// System.out.print(grbl.grblBfr.size());
		// System.out.print(", ");
		// System.out.print(grbl.reservedMsg.size());
	}

	public void exit() {
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
			setWritable(!tabletInput.isWritable());
		}
		else if (key == 'h' || key == 'H') // set home
		{
			homeCmdTrigger();
			waitingTimeMsec = 500;
		}
		else if (key == 'w' || key == 'W') // servo up
		{
			strBfr.append("M3")//
					.append("S").append(Setting.servoHover)//
					.append('\r');
			grbl.reserve(strBfr.toString());
			strBfr.setLength(0);
		}
		else if (key == 's' || key == 'S') // servo down
		{
			strBfr.append("M3")//
					.append("S").append(Setting.servoZero)//
					.append('\r');
			grbl.reserve(strBfr.toString());
			strBfr.setLength(0);
		}
		else if (key == 'x' || key == 'X') // servo off
		{
			strBfr.append("M3")//
					.append("S0")//
					.append('\r');
			grbl.reserve(strBfr.toString());
			strBfr.setLength(0);
		}
	}

	String timestamp() {
		Calendar now = Calendar.getInstance();
		return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", now);
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { main.TH_DIS2018.class.getName() };
		if (passedArgs != null)
			PApplet.main(concat(appletArgs, passedArgs));
		else
			PApplet.main(appletArgs);
	}
}
