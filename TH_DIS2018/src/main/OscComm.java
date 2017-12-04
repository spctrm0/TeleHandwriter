package main;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

public class OscComm {
	// references
	public PApplet		p5					= null;
	public Strokes		strokes				= null;

	// settings
	public final String	addrPtrnRequest		= "request";
	public final String	addrPtrnReply		= "reply";
	public final String	addrPtrnDisconnect	= "disconnect";
	public final String	addrPtrnPenInput	= "penInput";
	public final String	addrPtrnCalibration	= "calibration";

	// parameters
	public boolean		isConnected			= false;

	// objects
	public OscP5		oscPort				= null;
	public NetAddress	myAddr				= null;
	public NetAddress	targetAddr			= null;
	public OscMessage	msg					= null;

	// buffers
	public StringBuffer	prtTxtBfr			= null;

	public void sendHandshake(String _addrPattern) {
		msg.clear();
		msg.setAddrPattern(_addrPattern);
		msg.add(oscPort.ip()).add(Setting.myPort);
		oscPort.send(msg, targetAddr);
	}

	public void toggleConnect(boolean _isConnected) {
		if (isConnected != _isConnected) {
			isConnected = _isConnected;
			if (isConnected) {
				prtTxtBfr.append("<OSC>").append('\t').append("Connected with ").append(Setting.targetIp).append(":")
						.append(Setting.targetPort);
			} else {
				sendHandshake(addrPtrnDisconnect);
				prtTxtBfr.append("<OSC>").append('\t').append("Disconnected");
			}
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
		}
	}

	public void closePort() {
		if (isConnected)
			toggleConnect(false);
		oscPort.stop();
		oscPort = null;
	}

	public void dispose() {
		if (oscPort != null)
			closePort();
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

	public OscComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("dispose", this);

		prtTxtBfr = new StringBuffer();

		msg = new OscMessage("");

		toggleConnect(false);
		openPort();
		setTargetAddr();
	}

	public void setStrokes(Strokes _strokes) {
		strokes = _strokes;
	}

	public void tryConnect() {
		toggleConnect(false);
		sendHandshake(addrPtrnRequest);
		prtTxtBfr.append("<OSC>").append('\t').append("Try to connect with ").append(Setting.targetIp).append(":")
				.append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public void receive(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(addrPtrnRequest)) {
			prtTxtBfr.append("<OSC>").append('\t').append("Got a connection request from ")
					.append(_oscMsg.get(0).stringValue()).append(":").append(_oscMsg.get(1).intValue());
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
			sendHandshake(addrPtrnReply);
			toggleConnect(true);
		} else if (_oscMsg.addrPattern().equals(addrPtrnReply)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp)
					&& _oscMsg.get(1).intValue() == Setting.targetPort) {
				prtTxtBfr.append("<OSC>").append('\t').append("Got a reply from ").append(_oscMsg.get(0).stringValue())
						.append(":").append(_oscMsg.get(1).intValue());
				System.out.println(prtTxtBfr);
				prtTxtBfr.setLength(0);
				toggleConnect(true);
			}
		} else if (_oscMsg.addrPattern().equals(addrPtrnDisconnect)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp)
					&& _oscMsg.get(1).intValue() == Setting.targetPort)
				toggleConnect(false);
		} else if (_oscMsg.addrPattern().equals(addrPtrnCalibration)) {
			prtTxtBfr.append("<OSC>").append('\t').append("Got calibration data");
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
			Setting.targetCalibX = _oscMsg.get(0).floatValue();
			Setting.targetCalibY = _oscMsg.get(1).floatValue();
			Setting.targetTabletWidth = _oscMsg.get(2).intValue();
			Setting.targetTabletHeight = _oscMsg.get(3).intValue();
			Setting.targetScreentWidth = _oscMsg.get(4).intValue();
			Setting.targetScreenHeight = _oscMsg.get(5).intValue();
		} else if (_oscMsg.addrPattern().equals(addrPtrnPenInput)) {
			switch (_oscMsg.get(0).intValue()) {
			case 1: // Press
				strokes.addStroke(_oscMsg.get(1).floatValue(), _oscMsg.get(2).floatValue(), _oscMsg.get(3).floatValue(),
						_oscMsg.get(4).floatValue(), _oscMsg.get(5).floatValue(),
						Long.parseLong(_oscMsg.get(6).stringValue()));
				break;
			case 4: // Drag
				strokes.addPoint(_oscMsg.get(1).floatValue(), _oscMsg.get(2).floatValue(), _oscMsg.get(3).floatValue(),
						_oscMsg.get(4).floatValue(), _oscMsg.get(5).floatValue(),
						Long.parseLong(_oscMsg.get(6).stringValue()));
				break;
			case 2: // Release
				strokes.addLastPoint(_oscMsg.get(1).floatValue(), _oscMsg.get(2).floatValue(),
						_oscMsg.get(3).floatValue(), _oscMsg.get(4).floatValue(), _oscMsg.get(5).floatValue(),
						Long.parseLong(_oscMsg.get(6).stringValue()));
				break;
			}
		}
	}

	public void sendCalibration(float _penX, float _penY, int _tabletWidth, int _tabletHeight, int _screenWidth,
			int _screenHeight) {
		msg.clear();
		msg.setAddrPattern(addrPtrnCalibration);
		msg.add(_penX);
		msg.add(_penY);
		msg.add(_tabletWidth);
		msg.add(_tabletHeight);
		msg.add(_screenWidth);
		msg.add(_screenHeight);
		oscPort.send(msg, targetAddr);
	}

	public void sendPenInput(int _action, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis) {
		msg.clear();
		msg.setAddrPattern(addrPtrnPenInput);
		msg.add(_action);
		msg.add(_penX);
		msg.add(_penY);
		msg.add(_pressure);
		msg.add(_tiltX);
		msg.add(_tiltY);
		msg.add(Long.toString(_millis));
		oscPort.send(msg, targetAddr);
	}
}