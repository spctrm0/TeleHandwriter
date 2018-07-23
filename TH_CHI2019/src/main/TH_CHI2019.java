package main;

import processing.core.PApplet;
import processing.serial.Serial;

public class TH_CHI2019 extends PApplet {
	SerialPortManager serialPortManager;
	GrblComm grbl;

	public void settings() {
		// fullScreen();
		size(800, 800);
	}

	public void setup() {
		serialPortManager = new SerialPortManager(this);
	}

	public void serialEvent(Serial _serial) {
		if (serialPortManager.getTempSerialPort() != null)
			serialPortManager.getTempSerialPort().charToStringAndCallback(_serial.readChar());
		else
			for (SerialPort serialPort_ : serialPortManager.getSerialPorts())
				if (_serial == serialPort_.getSerial())
					serialPort_.charToStringAndCallback(_serial.readChar());
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { main.TH_CHI2019.class.getName() };
		if (passedArgs != null)
			PApplet.main(concat(appletArgs, passedArgs));
		else
			PApplet.main(appletArgs);
	}
}
