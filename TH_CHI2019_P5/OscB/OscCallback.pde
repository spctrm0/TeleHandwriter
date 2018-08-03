public interface OscCallback {

  public void oscConnectionCallBack(OscTargetAddr _oscTargetAddr, boolean _isConnected);

  public void oscMsgCallBack(OscMessage _oscMsg);
}