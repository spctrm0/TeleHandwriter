package grbl;

import java.util.concurrent.TimeUnit;

import main.Setting;
import oscComm.OscComm;
import processing.core.PApplet;
import processing.serial.Serial;

public class SerialComm {
	private PApplet	p5			= null;
	private Grbl		grbl		= null;
	private OscComm	oscComm	= null;

	private final int		connectPeriodMsec	= 2000;
	private final int		baudRate					= 250000;
	private final char	parity						= 'n';
	private final int		dataBits					= 8;
	private final float	stopBits					= 1.0f;
	private final char	delimeter					= '\r';

	private long		connectTrialTimeUsec	= 0;
	private int			portIdx								= 0;
	private boolean	isConnected						= false;

	private Serial srlPort = null;

	private StringBuffer	charToStrBfr	= null;
	private StringBuffer	prtTxtBfr			= null;

	public void setGrbl(Grbl _grbl) {
		grbl = _grbl;
	}

	public void setOscComm(OscComm _oscComm) {
		oscComm = _oscComm;
	}

	public SerialComm(PApplet _p5) {
		p5 = _p5;

		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		charToStrBfr = new StringBuffer();
		prtTxtBfr = new StringBuffer();

		printSerialList();
	}

	public void pre() {
		tryConnectPeriodically(connectPeriodMsec);
	}

	private void tryConnectPeriodically(int _periodMsec) {
		if (!isConnected) {
			if (getTimePassedMsec() >= _periodMsec) {
				if (Serial.list().length > 0) {
					portIdx++;
					if (portIdx >= Serial.list().length)
						portIdx = 0;
					connect(portIdx);
				}
			}
		}
	}

	private long getTimePassedMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
	}

	private void connect(int _portIdx) {
		if (Serial.list().length > 0 && _portIdx < Serial.list().length) {
			String portName_ = Serial.list()[_portIdx];
			if (srlPort != null)
				srlPort.stop();
			srlPort = new Serial(p5, portName_, baudRate, parity, dataBits, stopBits);
			connectTrialTimeUsec = System.nanoTime();
			prtTxtBfr.append("<SRL>").append('\t').append("Try to connect with ").append(portName_);
			System.out.println(prtTxtBfr.toString());
			prtTxtBfr.setLength(0);
		}
	}

	public void dispose() {
		if (srlPort != null)
			disconnect();
	}

	private void disconnect() {
		srlPort.stop();
		srlPort = null;
		if (isConnected) {
			prtTxtBfr.append("<SRL>").append('\t').append("Disconnected");
			System.out.println(prtTxtBfr.toString());
			prtTxtBfr.setLength(0);
			setConnected(false);
		}
	}

	private void printSerialList() {
		prtTxtBfr.append("<SRL>").append('\t').append("PortList...").append('\n');
		for (int i = 0; i < Serial.list().length; i++) {
			String portName_ = Serial.list()[i];
			prtTxtBfr.append('\t').append("[").append(i).append("] ").append(portName_).append('\n');
		}
		System.out.print(prtTxtBfr.toString());
		prtTxtBfr.setLength(0);
	}

	public void write(String _msg) {
		srlPort.write(_msg);
	}

	public void read(char _char) {
		if (_char != delimeter)
			charToStrBfr.append(_char);
		else {
			String msg_ = charToStrBfr.toString().trim();
			if (!msg_.isEmpty()) {
				if (!isConnected) {
					if (msg_.equals(Setting.connectionChkTxt)) {
						boolean wasConnected_ = isConnected;
						setConnected(false);
						if (wasConnected_ != isConnected) {
							prtTxtBfr.append("<SRL>").append('\t').append("Connected with ").append(srlPort.port.getPortName())
									.append('\n');
							prtTxtBfr.append("<GRBL>").append('\t').append(msg_);
							System.out.println(prtTxtBfr.toString());
							prtTxtBfr.setLength(0);
						}
						grbl.init();
					}
				}
				else
					grbl.read(msg_);
			}
			charToStrBfr.setLength(0);
		}
	}

	public void activateAutoConnect() {
		disconnect();
		tryConnectPeriodically(connectPeriodMsec);
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean _isConnected) {
		isConnected = _isConnected;
		oscComm.updateSerialCommIsConnected(isConnected);
	}
}