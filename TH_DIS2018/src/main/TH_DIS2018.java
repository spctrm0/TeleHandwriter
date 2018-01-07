package main;

import drawing.Stroke;
import grbl.GrblComm;
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
	TabletInput tabletInput;

	SerialComm	serialComm;
	GrblComm		grblComm;

	OscComm oscComm;

	Drawing drawing;

	Table				table;
	Interpreter	interpreter;

	public void settings() {
		// fullScreen();
		size(800, 800);
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
				if (parsed_[0].equals("myCalibXInPx"))
					Setting.myCalibXInPx = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("myCalibYInPx"))
					Setting.myCalibYInPx = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("targetCalibXInPx"))
					Setting.targetCalibXInPx = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("targetCalibYInPx"))
					Setting.targetCalibYInPx = Float.parseFloat(parsed_[1]);
				if (parsed_[0].equals("servoHover"))
					Setting.servoHover = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoZero"))
					Setting.servoZero = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[0]"))
					Setting.servoDelay[0] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[1]"))
					Setting.servoDelay[1] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[2]"))
					Setting.servoDelay[2] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("servoDelay[3]"))
					Setting.servoDelay[3] = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("feedrateStrokeToStoke"))
					Setting.feedrateStrokeToStoke = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("xBackOff"))
					Setting.xBackOff = Float.parseFloat(parsed_[1]);
				else if (parsed_[0].equals("myPort"))
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
			}
		}
	}

	public void setup() {
		loadSetting();

		tabletInput = new TabletInput(this);

		serialComm = new SerialComm(this);
		grblComm = new GrblComm(this);

		oscComm = new OscComm(this);

		drawing = new Drawing();

		table = new Table();
		table.addColumn("nthPoint");
		table.addColumn("nthStroke");
		table.addColumn("nthPointInStroke");
		table.addColumn("penX");
		table.addColumn("penY");
		table.addColumn("cncX");
		table.addColumn("cncY");
		table.addColumn("feedrate");
		table.addColumn("pressure");
		table.addColumn("tiltX");
		table.addColumn("tiltY");
		table.addColumn("evtTimeInMsec");
		interpreter = new Interpreter(this);

		tabletInput.setOscComm(oscComm);

		serialComm.setGrblComm(grblComm);
		serialComm.setOscComm(oscComm);
		grblComm.setSerialComm(serialComm);
		grblComm.setOscComm(oscComm);

		oscComm.setDrawing(drawing);

		interpreter.setDrawing(drawing);
		interpreter.setGrblComm(grblComm);
		interpreter.setTable(table);
	}

	public void draw() {
		background(serialComm.isConnected() ? 0 : 255, oscComm.isConnected() ? 0 : 255,
				(tabletInput.isWritable()) ? 0 : 255);
		noStroke();
		fill(oscComm.isTargetWritable() ? 0 : 255, oscComm.isTargetWritable() ? 255 : 0, 0);
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
		noStroke();
		fill(255);
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
		String cmd_;
		if (key == '~') {
			oscComm.activateAutoConnect();
		}
		else if (key == 'i' || key == 'I') // toggle writable
		{
			setWritable(!tabletInput.isWritable());
		}
		else if (key == 'h' || key == 'H') // set home
		{
			// grbl.activateAutoHome();
		}
		else if (key == 'w' || key == 'W') // servo up
		{
			cmd_ = "M3";
			cmd_ += 'S' + Setting.servoHover;
			cmd_ += '\r';
			grblComm.reserveCmd(cmd_);
		}
		else if (key == 's' || key == 'S') // servo down
		{
			cmd_ = "M3";
			cmd_ += 'S' + Setting.servoZero;
			cmd_ += '\r';
			grblComm.reserveCmd(cmd_);
		}
		else if (key == 'x' || key == 'X') // servo off
		{
			cmd_ = "M3";
			cmd_ += "S0";
			cmd_ += '\r';
			grblComm.reserveCmd(cmd_);
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
