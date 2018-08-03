public class OscTargetAddr {

  private LinkedList<OscCallback> listeners = null;

  public void addListener(OscCallback _listener) {
    if (!listeners.contains(_listener))
      listeners.addLast(_listener);
  }

  public void removeListener(OscCallback _listener) {
    if (listeners.contains(_listener))
      listeners.remove(_listener);
  }

  public boolean containsListener(OscCallback _listener) {
    return listeners.contains(_listener);
  }

  private PApplet p5 = null;
  private OscComm oscComm = null;

  private NetAddress targetAddr = null;
  private String targetIp = null;
  private int targetPort = 0;

  public String prefixConnectionSyn = "Syn";
  public String prefixConnectionSynAck = "SynAck";
  public String prefixConnectionAck = "Ack";
  public String prefixDisconnect = "Disconnect";

  private long lastConnectionAttemptInUsec = 0;
  private boolean isConnected = false;
  private boolean isConnecting = false;

  public int connectionAttemptIntervalInMSec = 2000;

  public OscTargetAddr(PApplet _p5, OscComm _oscComm, String _targetIp, int _targetPort) {
    p5 = _p5;
    oscComm = _oscComm;

    listeners = new LinkedList<OscCallback>();

    p5.registerMethod("dispose", this);

    targetIp = _targetIp;
    targetPort = _targetPort;
    targetAddr = new NetAddress(targetIp, targetPort);

    String log_ = "<" + getClass().getName() + ">\t" + targetIp + ":" + targetPort + " is created.";
    System.out.println(log_);
  }

  public void dispose() {
    setConnected(false, true);
  }

  public void pre() {
    if (getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec) {
      String log_ = "<" + getClass().getName() + ">\tSend (Syn) Msg to " + targetIp + ":" + targetPort + ".";
      System.out.println(log_);
      sendOscConnectionMsg(prefixConnectionSyn);
      setLastConnectionAttemptInUsec();
    }
  }

  public String getTargetIp() {
    return targetIp;
  }

  public int getTargetPort() {
    return targetPort;
  }

  public void setTargetAddr(String _targetIp, int _targetPort) {
    targetIp = _targetIp;
    targetPort = _targetPort;
    disconnect();
    targetAddr = new NetAddress(targetIp, targetPort);
    String log_ = "<" + getClass().getName() + ">\tSet to" + targetIp + ":" + targetPort + ".";
    System.out.println(log_);
  }

  public boolean isConnected() {
    return isConnected;
  }

  public boolean isConnecting() {
    return isConnecting;
  }

  public void tryConnect() {
    activateSendSynMsgPeriodically();
    String log_ = "<" + getClass().getName() + ">\tTry to connect with " + targetAddr.address() + ":"
      + targetAddr.port() + ".";
    System.out.println(log_);
  }

  public void disconnect() {
    setConnected(false, false);
  }

  public void write(OscMessage _oscMsg) {
    oscComm.getOscP5().send(_oscMsg);
  }

  protected void readAndCallback(OscMessage _oscMsg) {
    if (_oscMsg.addrPattern().equals(prefixConnectionSyn) || _oscMsg.addrPattern().equals(prefixConnectionSynAck)
      || _oscMsg.addrPattern().equals(prefixConnectionAck) || _oscMsg.addrPattern().equals(prefixDisconnect)) {
      if (_oscMsg.addrPattern().equals(prefixDisconnect))
        disconnect();
      else if (!isConnected) {
        String log_;
        if (_oscMsg.addrPattern().equals(prefixConnectionSyn)) {
          deactivateSendSynMsgPeriodically();
          log_ = "<" + getClass().getName() + ">\tGot a (Syn) Msg from " + targetIp + ":" + targetPort + ".\n";
          log_ += "<" + getClass().getName() + ">\tSend back (Syn + Ack) Msg to " + targetIp + ":" + targetPort + ".";
          System.out.println(log_);
          sendOscConnectionMsg(prefixConnectionSynAck);
        } else if (_oscMsg.addrPattern().equals(prefixConnectionSynAck)) {
          deactivateSendSynMsgPeriodically();
          log_ = "<" + getClass().getName() + ">\tGot a (Syn + Ack) Msg from " + targetIp + ":" + targetPort + ".\n";
          log_ += "<" + getClass().getName() + ">\tSend back (Ack) Msg to " + targetIp + ":" + targetPort + ".";
          System.out.println(log_);
          sendOscConnectionMsg(prefixConnectionAck);
          setConnected(true, false);
        } else if (_oscMsg.addrPattern().equals(prefixConnectionAck))
          setConnected(true, false);
      }
    } else
      for (OscCallback listener_ : listeners)
        listener_.oscMsgCallBack(_oscMsg);
  }

  private long getElapsedTimeSinceLastConnectionAttemptInMsec() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionAttemptInUsec);
  }

  private void sendOscConnectionMsg(String _prefixConnection) {
    OscMessage msg_ = new OscMessage(_prefixConnection);
    msg_.add(oscComm.getMyIp()).add(oscComm.getMyPort());
    oscComm.getOscP5().send(msg_, targetAddr);
  }

  private void setLastConnectionAttemptInUsec() {
    lastConnectionAttemptInUsec = System.nanoTime();
  }

  private void setConnected(boolean _isConnected, boolean _isForced) {
    boolean isChanged_ = _isConnected != isConnected;
    isConnected = _isConnected;
    String log_;
    if (isConnected && isChanged_) {
      for (OscCallback listener_ : listeners)
        listener_.oscConnectionCallBack(this, true);
      log_ = "<" + getClass().getName() + ">\tConnected with " + targetAddr.address() + ":" + targetAddr.port() + ".";
      System.out.println(log_);
    } else if (!isConnected) {
      if (isConnecting)
        deactivateSendSynMsgPeriodically();
      for (OscCallback listener_ : listeners)
        listener_.oscConnectionCallBack(this, false);
      if (!isConnecting && (isChanged_ || _isForced)) {
        log_ = "<" + getClass().getName() + ">\tDisconnected with " + targetAddr.address() + ":" + targetAddr.port()
          + ".";
        System.out.println(log_);
      }
    }
  }

  private void activateSendSynMsgPeriodically() {
    if (!isConnecting) {
      isConnecting = true;
      p5.registerMethod("pre", this);
    }
  }

  private void deactivateSendSynMsgPeriodically() {
    if (isConnecting) {
      isConnecting = false;
      p5.unregisterMethod("pre", this);
    }
  }
}