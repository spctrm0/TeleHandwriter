import oscP5.OscMessage;

OscComm    oscComm;

public void setup() {
  size(500, 500);
  oscComm = new OscComm(this);
}

public void draw() {
}

public void oscEvent(OscMessage _oscMsg) {
  oscComm.receive(_oscMsg);
}

public void keyPressed() {
  oscComm.tryConnect();
}