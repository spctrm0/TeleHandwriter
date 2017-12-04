import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

public class OscComm {
  
  int    myPort        = 9999;
  String  targetIp      = "192.168.0.2";
  int    targetPort      = 6666;
  
  // references
  public PApplet    p5          = null;

  // settings
  public final String  addrPtrnRequest    = "request";
  public final String  addrPtrnReply    = "reply";
  public final String  addrPtrnDisconnect  = "disconnect";
  public final String  addrPtrnPenInput  = "pen";

  // parameters
  public boolean    isConnected      = false;

  // objects
  public OscP5    oscPort        = null;
  public NetAddress  myAddr        = null;
  public NetAddress  targetAddr      = null;
  public OscMessage  msg          = null;

  // buffers
  public StringBuffer  prtTxtBfr      = null;

  public void sendHandshake(String _addrPattern) {
    msg.clear();
    msg.setAddrPattern(_addrPattern);
    msg.add(oscPort.ip()).add(myPort);
    oscPort.send(msg, targetAddr);
  }

  public void toggleConnect(boolean _toggleConnect) {
    if (isConnected != _toggleConnect) {
      isConnected = _toggleConnect;
      if (isConnected) {
        prtTxtBfr.append("<OSC>").append('\t').append("connected with ").append(targetIp).append(":")
            .append(targetPort);
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
    oscPort = new OscP5(p5, myPort);
  }

  public void setTargetAddr() {
    targetAddr = new NetAddress(targetIp, targetPort);
    prtTxtBfr.append("<OSC>").append('\t').append("Target address...").append('\n');
    prtTxtBfr.append('\t').append(targetIp).append(":").append(targetPort);
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

  public void tryConnect() {
    toggleConnect(false);
    sendHandshake(addrPtrnRequest);
    prtTxtBfr.append("<OSC>").append('\t').append("Try to connect with ").append(targetIp).append(":")
        .append(targetPort);
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
      if (_oscMsg.get(0).stringValue().equals(targetIp)
          && _oscMsg.get(1).intValue() == targetPort) {
        prtTxtBfr.append("<OSC>").append('\t').append("Got a reply from ").append(_oscMsg.get(0).stringValue())
            .append(":").append(_oscMsg.get(1).intValue());
        System.out.println(prtTxtBfr);
        prtTxtBfr.setLength(0);
        toggleConnect(true);
      }
    } else if (_oscMsg.addrPattern().equals(addrPtrnDisconnect)) {
      if (_oscMsg.get(0).stringValue().equals(targetIp)
          && _oscMsg.get(1).intValue() == targetPort)
        toggleConnect(false);
    } else if (_oscMsg.addrPattern().equals(addrPtrnPenInput)) {
      System.out.println(_oscMsg.typetag());
    }
  }

  public void sendPenInput(int _action, float _penX, float _penY, float _tiltX, float _tiltY, long _millis) {
    msg.clear();
    msg.setAddrPattern(addrPtrnPenInput);
    msg.add(_action);
    msg.add(_penX);
    msg.add(_penY);
    msg.add(_tiltX);
    msg.add(_tiltY);
    msg.add(Long.toString(_millis));
    oscPort.send(msg, targetAddr);
  }
}