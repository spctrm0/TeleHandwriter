package oscComm;

import java.util.concurrent.TimeUnit;

import drawing.Drawing;
import main.Setting;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.serial.Serial;

public class OscComm {
	public PApplet	p5			= null;
	public Drawing	drawing	= null;

	public final int		connectIntervalMsec	= 3000;
	public final String	addrPtrnRequest			= "request";
	public final String	addrPtrnReply				= "reply";
	public final String	addrPtrnDisconnect	= "disconnect";
	public final String	addrPtrnTabletInput	= "tabletInput";

	public long			connectTrialTimeUsec	= 0;
	public boolean	isConnected						= false;

	public OscP5			oscPort			= null;
	public NetAddress	myAddr			= null;
	public NetAddress	targetAddr	= null;
	public OscMessage	msg					= null;

	public StringBuffer prtTxtBfr = null;

	public void setDrawing(Drawing _drawing) {
		drawing = _drawing;
	}

	public OscComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		prtTxtBfr = new StringBuffer();

		msg = new OscMessage("");

		openPort();
		setTargetAddr();
	}

	public void pre() {
		if (!isConnected)
			connecting(connectIntervalMsec);
	}

	public void connecting(int _tryIntervalMsec) {
		if (getWaitingTimeMsec() >= _tryIntervalMsec) {
			tryToConnect();
			connectTrialTimeUsec = System.nanoTime();
		}
	}

	public long getWaitingTimeMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
	}

	public void tryToConnect() {
		disconnect(true);
		sendConnectionMsg(addrPtrnRequest);
		prtTxtBfr.append("<OSC>").append('\t').append("Try to connect with ").append(Setting.targetIp).append(":")
				.append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public void disconnect(boolean _doSend) {
		if (isConnected) {
			prtTxtBfr.append("<OSC>").append('\t').append("Disconnected");
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
			isConnected = false;
			sendConnectionMsg(addrPtrnDisconnect);
		}
		else if (_doSend) {
			sendConnectionMsg(addrPtrnDisconnect);
		}
	}

	public void sendConnectionMsg(String _addrPattern) {
		msg.clear();
		msg.setAddrPattern(_addrPattern);
		msg.add(oscPort.ip()).add(Setting.myPort);
		oscPort.send(msg, targetAddr);
	}

	public void dispose() {
		if (oscPort != null)
			closePort();
	}

	public void closePort() {
		disconnect(false);
		oscPort.stop();
		oscPort = null;
	}

	public void openPort() {
		if (oscPort != null)
			oscPort.stop();
		oscPort = new OscP5(p5, Setting.myPort);
	}

	public void setTargetAddr() {
		targetAddr = new NetAddress(Setting.targetIp, Setting.targetPort);
		prtTxtBfr.append("<OSC>").append('\t').append("Target address...").append('\n');
		prtTxtBfr.append('\t').append(Setting.targetIp).append(":").append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public void sendTabletInputMsg(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY,
			float _pressure, float _tiltX, float _tiltY, long _millis, int _kind) {
		msg.clear();
		msg.setAddrPattern(addrPtrnTabletInput);
		msg.add(_totalPointIdx);
		msg.add(_strokeIdx);
		msg.add(_pointIdx);
		msg.add(_penX);
		msg.add(_penY);
		msg.add(_pressure);
		msg.add(_tiltX);
		msg.add(_tiltY);
		msg.add(Long.toString(_millis));
		msg.add(_kind);
		oscPort.send(msg, targetAddr);
	}

	public void receive(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(addrPtrnRequest)) {
			prtTxtBfr.append("<OSC>").append('\t').append("Got a connection request from ")
					.append(_oscMsg.get(0).stringValue()).append(":").append(_oscMsg.get(1).intValue());
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
			sendConnectionMsg(addrPtrnReply);
			setToConnect();
		}
		else if (_oscMsg.addrPattern().equals(addrPtrnReply)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp) && _oscMsg.get(1).intValue() == Setting.targetPort) {
				prtTxtBfr.append("<OSC>").append('\t').append("Got a reply from ").append(_oscMsg.get(0).stringValue())
						.append(":").append(_oscMsg.get(1).intValue());
				System.out.println(prtTxtBfr);
				prtTxtBfr.setLength(0);
				setToConnect();
			}
		}
		else if (_oscMsg.addrPattern().equals(addrPtrnDisconnect)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp) && _oscMsg.get(1).intValue() == Setting.targetPort)
				disconnect(false);
		}
		else if (_oscMsg.addrPattern().equals(addrPtrnTabletInput)) {
			if (_oscMsg.get(9).intValue() == 0)
				drawing.addStroke(_oscMsg.get(0).intValue(), _oscMsg.get(1).intValue(), _oscMsg.get(2).intValue(),
						_oscMsg.get(3).floatValue(), _oscMsg.get(4).floatValue(), _oscMsg.get(5).floatValue(),
						_oscMsg.get(6).floatValue(), _oscMsg.get(7).floatValue(), Long.parseLong(_oscMsg.get(8).stringValue()),
						_oscMsg.get(0).intValue());
			else
				drawing.addPoint(_oscMsg.get(0).intValue(), _oscMsg.get(1).intValue(), _oscMsg.get(2).intValue(),
						_oscMsg.get(3).floatValue(), _oscMsg.get(4).floatValue(), _oscMsg.get(5).floatValue(),
						_oscMsg.get(6).floatValue(), _oscMsg.get(7).floatValue(), Long.parseLong(_oscMsg.get(8).stringValue()),
						_oscMsg.get(0).intValue());
		}
	}

	public void setToConnect() {
		if (!isConnected) {
			isConnected = true;
			prtTxtBfr.append("<OSC>").append('\t').append("Connected with ").append(Setting.targetIp).append(":")
					.append(Setting.targetPort);

			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
		}
	}
}