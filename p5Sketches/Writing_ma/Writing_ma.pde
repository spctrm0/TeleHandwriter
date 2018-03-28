import controlP5.*; //<>//

ControlP5 cp5;
boolean gate = false;

PVector mouse = new PVector();
PVector pmouse = new PVector();
ArrayList<PVector> mouseList = new ArrayList<PVector>();
int sampleSize = 8;
PVector pos = new PVector();
PVector ppos = new PVector();
boolean isLine = false;

PGraphics pg;

public void setup() {
  size(800, 800);
  pg = createGraphics(width, height);
  pg.beginDraw();
  pg.background(255);
  pg.endDraw();
  cp5 = new ControlP5(this);
}

public void draw() {
  pg.beginDraw();
  pg.noStroke();
  pg.fill(255, 4);
  pg.rect(0, 0, pg.width, pg.height);
  pg.endDraw();
  image(pg, 0, 0);
  noStroke();
  fill(0);
  if (pos != null) {
    ellipse(pos.x, pos.y, 30, 30);
  } else {
    ellipse(ppos.x, ppos.y, 30, 30);
  }
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
    isLine = false;
    mouseList.clear();
    pmouse.set(mouseX, mouseY);
    mouse.set(mouseX, mouseY);
    mouseList.add(new PVector(mouse.x, mouse.y));
  }
}

public void drawTrail() {
  if (gate) {
    mouse.set(mouseX, mouseY);
    mouseList.add(new PVector(mouse.x, mouse.y));
    pos = getAverage(sampleSize);
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
    if (pos != null) {
      if (!isLine) {
        isLine = true;
      } else {
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
      }
      ppos.set(pos);
    }
    pmouse.set(mouseX, mouseY);
  }
}

public PVector getAverage(int _sampleSize) {
  if (mouseList.size() >= _sampleSize) {
    while (mouseList.size() > _sampleSize) {
      mouseList.remove(0);
    }
    PVector average_ = new PVector(0, 0);
    for (PVector mouse_ : mouseList)
      average_.add(mouse_);
    average_.div(_sampleSize);
    return average_;
  }
  return null;
}

public void mouseDragged() {
  drawTrail();
}
public void mouseReleased() {
  drawTrail();
}