package main;

import drawing.Stroke;
import grbl.GrblComm;
import grbl.Interpreter;
import grbl.SerialComm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

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

	public void loadSetting() {
		ArrayList<Field> fields_ = new ArrayList<Field>(Arrays.asList(Setting.class.getDeclaredFields()));
		String[] lines_ = loadStrings("Setting.txt");
		System.out.println("<SYS>\tLoad setting...");
		for (String line_ : lines_) {
			String[] parsed_ = line_.split("=");
			if (parsed_.length >= 2) {
				parsed_[0] = parsed_[0].trim();
				parsed_[1] = parsed_[1].trim();
				for (int i = fields_.size() - 1; i >= 0; i--) {
					Field field_ = fields_.get(i);
					String fieldName_ = field_.getName();
					String fieldType_ = field_.getType().getTypeName();
					if (parsed_[0].equals(fieldName_)) {
						try {
							if (fieldType_.equals("boolean"))
								field_.set(null, Boolean.parseBoolean(parsed_[1]));
							else if (fieldType_.equals("int"))
								field_.set(null, Integer.parseInt(parsed_[1]));
							else if (fieldType_.equals("float"))
								field_.set(null, Float.parseFloat(parsed_[1]));
							else if (fieldType_.equals("java.lang.String"))
								field_.set(null, parsed_[1]);
							System.out.println("\t:" + fieldName_ + " = " + Setting.class.getDeclaredField(fieldName_).get(null));
							fields_.remove(i);
						}
						catch (Exception e) {
							System.out.println("\t" + e.toString());
						}
						break;
					}
				}
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
			tabletInput.setWritable(!tabletInput.isWritable());
		}
		else if (key == 'h' || key == 'H') // set home
		{
			grblComm.activateAutoHome();
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
