package main;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

public class OscComm {
	// references
	public PApplet		p5					= null;

	// settings
	public final String	addrPtrnAsk			= "ask";
	public final String	addrPtrnRpl			= "reply";
	public final String	addrPtrnRplBack		= "replyBack";
	public final String	addrPtrnDisconnect	= "disconnect";

	// parameters
	public boolean		isConnected			= false;
	public String		myIp				= null;

	// objects
	public OscP5		oscPort				= null;
	public NetAddress	myAddr				= null;
	public NetAddress	targetAddr			= null;
	public OscMessage	msg					= null;

	// buffers
	public StringBuffer	prtTxtBfr			= null;

	public void sendConnectionMsg(String _addrPattern) {
		msg.clear();
		msg.setAddrPattern(_addrPattern);
		msg.add(myIp).add(Setting.myPort);
		oscPort.send(msg, targetAddr);
	}

	public void toggleConnect(boolean _toggleConnect) {
		if (isConnected != _toggleConnect) {
			isConnected = _toggleConnect;
			if (isConnected) {
				prtTxtBfr.append("<OSC> Connected with ").append(Setting.targetIp).append(":")
						.append(Setting.targetPort);
			} else {
				sendConnectionMsg(addrPtrnDisconnect);
				prtTxtBfr.append("<OSC> Disconnected");
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
		toggleConnect(false);
		if (oscPort != null)
			oscPort.stop();
		oscPort = new OscP5(p5, Setting.myPort);
	}

	public void setMyIp() {
		prtTxtBfr.append("<OSC> My address...").append('\n');
		try {
			myIp = Inet4Address.getLocalHost().getHostAddress();
			if (myIp != null) {
				if (!myIp.equals("127.0.0.1"))
					prtTxtBfr.append('\t').append(myIp).append(":").append(Setting.myPort);
				else
					prtTxtBfr.append('\t').append("127.0.0.1").append(":").append(Setting.myPort);
			} else
				prtTxtBfr.append('\t').append("Error: Null");
		} catch (UnknownHostException e) {
			prtTxtBfr.append('\t').append("Error: UnknownHost");
		}
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
		if (myIp != null)
			openPort();
	}

	public void setTargetAddr() {
		toggleConnect(false);
		targetAddr = new NetAddress(Setting.targetIp, Setting.targetPort);
		prtTxtBfr.append("<OSC> Target address...").append('\n');
		prtTxtBfr.append('\t').append(Setting.targetIp).append(":").append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public OscComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("dispose", this);

		prtTxtBfr = new StringBuffer();

		msg = new OscMessage("");

		setMyIp();
		setTargetAddr();
	}

	public void tryConnect() {
		toggleConnect(false);
		sendConnectionMsg(addrPtrnAsk);
		prtTxtBfr.append("<OSC> Try to connect with ").append(Setting.targetIp).append(":").append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public void receive(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(addrPtrnAsk)) {
			toggleConnect(false);
			sendConnectionMsg(addrPtrnRpl);
			prtTxtBfr.append("<OSC> Got connection request from ").append(_oscMsg.get(0).stringValue()).append(":")
					.append(_oscMsg.get(1).intValue()).append('\n');
			prtTxtBfr.append("Reply back...");
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
		} else if (_oscMsg.addrPattern().equals(addrPtrnRpl)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp)
					&& _oscMsg.get(1).intValue() == Setting.targetPort) {
				sendConnectionMsg(addrPtrnRplBack);
				toggleConnect(true);
			}
		} else if (_oscMsg.addrPattern().equals(addrPtrnRplBack)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp)
					&& _oscMsg.get(1).intValue() == Setting.targetPort)
				toggleConnect(true);
		} else if (_oscMsg.addrPattern().equals(addrPtrnDisconnect)) {
			if (_oscMsg.get(0).stringValue().equals(Setting.targetIp)
					&& _oscMsg.get(1).intValue() == Setting.targetPort)
				toggleConnect(false);
		} else {

		}
	}
}
