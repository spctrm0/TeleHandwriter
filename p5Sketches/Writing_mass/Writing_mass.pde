import controlP5.*; //<>//

ControlP5 cp5;
boolean gate = false;

PVector mouse = new PVector();
PVector pmouse = new PVector();

public PVector force() {
  return PVector.sub(mouse, pmouse);
}

PVector pos = new PVector();
PVector ppos = new PVector();
PVector vel = new PVector();
PVector acc = new PVector();
float mass = 2;
float damping = 0.5f;

public void update(PVector _force) {
  ppos.set(pos);
  acc.set(_force);
  acc.div(mass);
  vel.add(acc);
  pos.add(vel);
  vel.mult(damping);
}

PGraphics pg;

public void setup() {
  size(800, 800);
  pg = createGraphics(width, height);
  pg.beginDraw();
  pg.background(255);
  pg.endDraw();
  cp5 = new ControlP5(this);
  cp5.addSlider("mass").setRange(0.5f, 2).linebreak();
  cp5.addSlider("damping").setRange(0, 1);
}

public void draw() {
  //background(255);
  pg.beginDraw();
  pg.noStroke();
  pg.fill(255, 4);
  pg.rect(0, 0, pg.width, pg.height);
  pg.endDraw();
  image(pg, 0, 0);
  noStroke();
  fill(0);
  ellipse(pos.x, pos.y, 30, 30);
  fill(0, 255, 0);
  ellipse(pmouse.x, pmouse.y, 10, 10);
  ellipse(mouse.x, mouse.y, 10, 10);
  noFill();
  stroke(0, 255, 0);
  line(pmouse.x, pmouse.y, mouse.x, mouse.y);
}

public void keyPressed() {
  if (key == ' ') {
    pg.clear();
    pg.beginDraw();
    pg.background(255);
    pg.endDraw();
  }
}

public void mousePressed() {
  gate = !cp5.isMouseOver();
  if (gate) {
    pmouse.set(mouseX, mouseY);
    pos.set(mouseX, mouseY);
    acc.set(0, 0);
    vel.set(0, 0);
  }
}

public void drawTrail() {
  if (gate) {
    mouse.set(mouseX, mouseY);
    //pre-refined
    pg.beginDraw();
    pg.noStroke();
    pg.fill(255, 0, 0);
    //pg.ellipse(pmouse.x, pmouse.y, 10, 10);
    //pg.ellipse(mouse.x, mouse.y, 10, 10);
    pg.noFill();
    pg.stroke(255, 0, 0);
    pg.line(pmouse.x, pmouse.y, mouse.x, mouse.y);
    pg.endDraw();
    update(force());
    //refined
    pg.beginDraw();
    pg.noStroke();
    pg.fill(0, 0, 255);
    //pg.ellipse(ppos.x, ppos.y, 10, 10);
    //pg.ellipse(pos.x, pos.y, 10, 10);
    pg.noFill();
    pg.stroke(0, 0, 255);
    pg.line(ppos.x, ppos.y, pos.x, pos.y);
    pg.endDraw();
    pmouse.set(mouseX, mouseY);
  }
}

public void mouseDragged() {
  drawTrail();
}
public void mouseReleased() {
  drawTrail();
}