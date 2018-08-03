import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

OscPort[] oscPort = {null, null};

public void setup() {
  oscPort[0] = new OscPort(this, 12000);
  oscPort[0].tryConnectWithAddr("127.0.0.1", 12001);
  oscPort[1] = new OscPort(this, 12001);
  oscPort[1].tryConnectWithAddr("127.0.0.1", 12000);
}

public void draw() {
}

public void mousePressed() {
  if(mouseButton == LEFT) {
  } else if(mouseButton == RIGHT) {
  }
}

public void oscEvent(OscMessage _oscMsg) {
  
}