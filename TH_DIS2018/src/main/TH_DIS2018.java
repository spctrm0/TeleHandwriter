package main;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.serial.Serial;

public class TH_DIS2018 extends PApplet {
	SerialComm	serialComm;
	KeyInput	keyInput;
	Grbl		grbl;
	TabletInput	tabletInput;
	OscComm		oscComm;

	public void settings() {
		fullScreen(1);
	}

	public void setup() {
		serialComm = new SerialComm(this);
		keyInput = new KeyInput(this);
		grbl = new Grbl();
		tabletInput = new TabletInput(this);
		oscComm = new OscComm(this);

		serialComm.setGrbl(grbl);

		textAlign(CENTER);

		thread("SerialCommThread");
	}

	public void draw() {
		background(serialComm.isConnected ? 0 : 255);
		noStroke();
		fill(255);
		text(keyInput.charToStrBfr.toString(), width / 2.0f, height / 2.0f);
	}

	public void serialEvent(Serial _serialEvt) {
		char replyChar_ = _serialEvt.readChar();
		serialComm.read(replyChar_);
	}

	public void oscEvent(OscMessage _oscMsg) {
		oscComm.receive(_oscMsg);
	}

	public void SerialCommThread() {
		while (!serialComm.isConnected) {
			serialComm.pre();
		}
	}
	
	public void mousePressed() {
		oscComm.tryConnect();
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { main.TH_DIS2018.class.getName() };
		if (passedArgs != null)
			PApplet.main(concat(appletArgs, passedArgs));
		else
			PApplet.main(appletArgs);
	}
}
