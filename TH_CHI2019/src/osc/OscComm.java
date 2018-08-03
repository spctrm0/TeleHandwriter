package osc;

import java.util.ArrayList;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

public class OscComm {

	private PApplet p5 = null;

	private OscP5		oscP5		= null;
	private String	myIp		= null;
	private int			myPort	= 0;

	public ArrayList<OscTargetAddr> targetAddrs;

	public OscComm(PApplet _p5, int _myPort) {
		p5 = _p5;

		targetAddrs = new ArrayList<OscTargetAddr>();

		myPort = _myPort;
		oscP5 = new OscP5(p5, _myPort);
		myIp = oscP5.ip();

		String log_ = "<" + getClass().getName() + ">\t" + myIp + ":" + myPort + " is created.";
		System.out.println(log_);
	}

	public OscP5 getOscP5() {
		return oscP5;
	}

	public String getMyIp() {
		return myIp;
	}

	public int getMyPort() {
		return myPort;
	}

	public void setMyAddr(int _myPort) {
		myPort = _myPort;
		disconnect();
		oscP5 = new OscP5(p5, _myPort);
		myIp = oscP5.ip();
		String log_ = "<" + getClass().getName() + ">\tSet to" + myIp + ":" + myPort + ".";
		System.out.println(log_);
	}

	public OscTargetAddr addTargetAddr(String _targetIp, int _targetPort) {
		if (!targetPortsContains(_targetIp, _targetPort)) {
			OscTargetAddr oscTargetAddr_ = new OscTargetAddr(p5, this, _targetIp, _targetPort);
			targetAddrs.add(oscTargetAddr_);
			String log_ = "<" + getClass().getName() + ">\t" + oscTargetAddr_.getTargetIp() + ":"
					+ oscTargetAddr_.getTargetPort() + " is added.";
			System.out.println(log_);
			return oscTargetAddr_;
		}
		return null;
	}

	public void removeTargetPorts(String _targetIp, int _targetPort) {
		OscTargetAddr oscTargetAddr_ = getTargetPort(_targetIp, _targetPort);
		if (oscTargetAddr_ != null) {
			targetAddrs.remove(oscTargetAddr_);
			String log_ = "<" + getClass().getName() + ">\t" + oscTargetAddr_.getTargetIp() + ":"
					+ oscTargetAddr_.getTargetPort() + " is removed.";
			System.out.println(log_);
		}
	}

	public boolean targetPortsContains(String _targetIp, int _targetPort) {
		for (OscTargetAddr oscTargetAddr_ : targetAddrs) {
			if (_targetIp.equals(oscTargetAddr_.getTargetIp()) && _targetPort == oscTargetAddr_.getTargetPort())
				return true;
		}
		return false;
	}

	public OscTargetAddr getTargetPort(String _targetIp, int _targetPort) {
		for (OscTargetAddr oscTargetAddr_ : targetAddrs) {
			if (_targetIp.equals(oscTargetAddr_.getTargetIp()) && _targetPort == oscTargetAddr_.getTargetPort())
				return oscTargetAddr_;
		}
		return null;
	}

	public void tryConnect() {
		for (OscTargetAddr oscTargetAddr_ : targetAddrs)
			oscTargetAddr_.tryConnect();
	}

	public void disconnect() {
		for (OscTargetAddr oscTargetAddr_ : targetAddrs)
			oscTargetAddr_.disconnect();
	}

	public void distributeToEachTargetAddr(OscMessage _oscMsg) {
		String targetIp_ = _oscMsg.netAddress().address();
		int targetPort_ = _oscMsg.netAddress().port();
		OscTargetAddr oscTargetAddr_ = getTargetPort(targetIp_, targetPort_);
		if (oscTargetAddr_ != null)
			oscTargetAddr_.readAndCallback(_oscMsg);
	}
}
