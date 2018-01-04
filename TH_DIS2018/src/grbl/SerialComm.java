package grbl;

import java.util.concurrent.TimeUnit;

import main.Setting;
import oscComm.OscComm;
import processing.core.PApplet;
import processing.serial.Serial;

public class SerialComm {
	private PApplet		p5				= null;
	private GrblComm	grblComm	= null;
	private OscComm		oscComm		= null;

	private final int		connectionTryPeriodInMSec	= 2000;
	private final int		baudRate									= 250000;
	private final char	parity										= 'n';
	private final int		dataBits									= 8;
	private final float	stopBits									= 1.0f;
	private final char	delimeter									= '\r';

	private long		lastConnectionTryTimeInUsec	= 0;
	private int			portIdx											= 0;
	private boolean	isConnected									= false;

	private Serial serial = null;

	private StringBuilder charToStrBfr;

	public void setGrblComm(GrblComm _grblComm) {
		grblComm = _grblComm;
	}

	public void setOscComm(OscComm _oscComm) {
		oscComm = _oscComm;
	}

	public SerialComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		charToStrBfr = new StringBuilder();

		printSerialList();
	}

	public void pre() {
		tryConnectPeriodically();
	}

	private void tryConnectPeriodically() {
		if (!isConnected) {
			if (timeSinceLastConnectionTryInMsec() >= connectionTryPeriodInMSec) {
				if (Serial.list().length > 0) {
					portIdx++;
					if (portIdx >= Serial.list().length)
						portIdx = 0;
					tryConnect(portIdx);
				}
			}
		}
	}

	private long timeSinceLastConnectionTryInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionTryTimeInUsec);
	}

	private void tryConnect(int _portIdx) {
		if (Serial.list().length > 0 && _portIdx < Serial.list().length) {
			String portName_ = Serial.list()[_portIdx];
			setConnect(false, false);
			serial = new Serial(p5, portName_, baudRate, parity, dataBits, stopBits);
			lastConnectionTryTimeInUsec = System.nanoTime();
			String print_ = "<SRL>\tTry to connect with ";
			print_ += portName_;
			System.out.println(print_);
		}
	}

	public void setConnect(boolean _isConnected, boolean _isForeced) {
		if ((_isConnected != isConnected) || _isForeced) {
			if (_isConnected != isConnected)
				oscComm.updateSerialCommIsConnected(!isConnected);
			isConnected = _isConnected;
			String print_;
			if (isConnected) {
				print_ = "<SRL>\tConnected with ";
				print_ += serial.port.getPortName();
				System.out.println(print_);
			}
			else {
				if (serial != null) {
					serial.clear();
					serial.stop();
				}
				print_ = "<SRL>\tDisconnected";
				System.out.println(print_);
			}
		}
	}

	public void dispose() {
		disposeSerial();
	}

	private void disposeSerial() {
		setConnect(false, true);
		if (serial != null) {
			serial.dispose();
			serial = null;
		}
	}

	private void printSerialList() {
		String print_ = "<SRL>\tPortList...\n";
		for (int i = 0; i < Serial.list().length; i++) {
			String portName_ = Serial.list()[i];
			print_ += "\t[";
			print_ += i;
			print_ += "] ";
			print_ += portName_;
			print_ += '\n';
		}
		System.out.print(print_);
	}

	public void write(String _msg) {
		serial.write(_msg);
	}

	public void read(char _char) {
		if (_char != delimeter)
			charToStrBfr.append(_char);
		else {
			String msg_ = charToStrBfr.toString().trim();
			if (!msg_.isEmpty()) {
				if (!isConnected) {
					if (msg_.equals(Setting.connectionChkTxt)) {
						setConnect(true, false);
						grblComm.init();
					}
				}
				else
					grblComm.read(msg_);
			}
			charToStrBfr.setLength(0);
		}
	}

	public void activateAutoConnect() {
		setConnect(false, false);
		tryConnectPeriodically();
	}

	public boolean isConnected() {
		return isConnected;
	}
}