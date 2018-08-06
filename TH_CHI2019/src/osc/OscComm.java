package osc;

import java.util.ArrayList;

import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscProperties;
import processing.core.PApplet;

public class OscComm {

	private PApplet p5 = null;

	private OscP5		oscP5	= null;
	private String	ip		= null;
	private int			port	= 0;

	private ArrayList<OscTarget> oscTargets;

	public static final int datagramSize = 4096;

	public OscComm(PApplet _p5, int _port) {
		p5 = _p5;

		oscTargets = new ArrayList<OscTarget>();

		p5.registerMethod("dispose", this);

		port = _port;
		OscProperties op_ = new OscProperties();
		op_.setListeningPort(port);
		op_.setDatagramSize(datagramSize);
		oscP5 = new OscP5(p5, op_);
		ip = oscP5.ip();
		String log_ = "<" + getClass().getName() + ">\t" + ip + ":" + port + " is opened.";
		System.out.println(log_);
	}

	public void dispose() {
		disconnectAll();
	}

	public OscP5 getOscP5() {
		return oscP5;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public void setAddr(int _port) {
		disconnectAll();
		port = _port;
		OscProperties op_ = new OscProperties();
		op_.setListeningPort(port);
		op_.setDatagramSize(datagramSize);
		oscP5 = new OscP5(p5, op_);
		ip = oscP5.ip();
		String log_ = "<" + getClass().getName() + ">\tSet to" + ip + ":" + port + ".";
		System.out.println(log_);
	}

	public OscTarget addOscTarget(String _ip, int _port) {
		if (!oscTargetsContains(_ip, _port)) {
			OscTarget oscTarget_ = new OscTarget(p5, this, _ip, _port);
			oscTargets.add(oscTarget_);
			String log_ = "<" + getClass().getName() + ">\t" + oscTarget_.getIp() + ":" + oscTarget_.getPort() + " is added.";
			System.out.println(log_);
			return oscTarget_;
		}
		return null;
	}

	public void removeOscTarget(String _ip, int _port) {
		OscTarget oscTarget_ = getOscTarget(_ip, _port);
		if (oscTarget_ != null) {
			oscTargets.remove(oscTarget_);
			String log_ = "<" + getClass().getName() + ">\t" + oscTarget_.getIp() + ":" + oscTarget_.getPort()
					+ " is removed.";
			System.out.println(log_);
		}
	}

	public boolean oscTargetsContains(String _ip, int _port) {
		for (OscTarget oscTarget_ : oscTargets) {
			if (_ip.equals(oscTarget_.getIp()) && _port == oscTarget_.getPort())
				return true;
		}
		return false;
	}

	public OscTarget getOscTarget(String _ip, int _port) {
		for (OscTarget oscTarget_ : oscTargets) {
			if (_ip.equals(oscTarget_.getIp()) && _port == oscTarget_.getPort())
				return oscTarget_;
		}
		return null;
	}

	public void tryConnectAll() {
		for (OscTarget oscTarget_ : oscTargets)
			oscTarget_.tryConnect();
	}

	public void disconnectAll() {
		for (OscTarget oscTarget_ : oscTargets)
			oscTarget_.disconnect();
	}

	public void distributeToEachOscTarget(OscMessage _oscMsg) {
		String msgTypetag_ = _oscMsg.typetag();
		int msgLegnth_ = msgTypetag_.length();
		int targetPort_ = _oscMsg.get(msgLegnth_ - 1).intValue();
		String targetIp_ = _oscMsg.get(msgLegnth_ - 2).stringValue();
		OscTarget oscTargetAddr_ = getOscTarget(targetIp_, targetPort_);
		if (oscTargetAddr_ != null)
			oscTargetAddr_.readAndCallback(_oscMsg);
	}
}