package osc;

import oscP5.OscMessage;

public interface OscCallback {

	public void oscConnectionCallBack(OscTarget _oscTarget, boolean _isConnected);

	public void oscMsgCallBack(OscMessage _oscMsg);
}
