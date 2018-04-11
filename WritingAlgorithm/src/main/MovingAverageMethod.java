package main;

import java.util.ArrayList;

import main.TabletInput.ReceivingPoint;
import processing.core.PApplet;
import processing.core.PVector;

public class MovingAverageMethod extends Drawing implements ReceivingPoint {
	private PVector							mouse				= new PVector();
	private PVector							pmouse			= new PVector();
	private PVector							pos					= new PVector();
	private PVector							ppos				= new PVector();
	private ArrayList<PVector>	mouseList		= new ArrayList<PVector>();
	private int									sampleSize	= 8;

	public MovingAverageMethod(PApplet _p5, int w, int h) {
		super(_p5, w, h);
	}

	private void addMouse(PVector _mouse) {
		mouseList.add(_mouse);
		if (mouseList.size() > sampleSize)
			mouseList.remove(0);
	}

	private PVector movingAverage() {
		PVector movingAverage_ = new PVector(0, 0);
		for (PVector mouse_ : mouseList)
			movingAverage_.add(mouse_);
		movingAverage_.div(mouseList.size());
		return movingAverage_;
	}

	private void update(PVector _movingAverage) {
		ppos.set(pos.x, pos.y);
		pos.set(movingAverage());
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	@Override
	public void receivePoint(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure,
			float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
		if (_nthPointInStroke == 1) {
			pmouse.set(_x, _y);
			pos.set(_x, _y);
			mouseList.clear();
			addMouse(new PVector(pmouse.x, pmouse.y));
		}
		else {
			mouse.set(_x, _y);
			addMouse(new PVector(mouse.x, mouse.y));
			update(movingAverage());
			pmouse.set(mouse.x, mouse.y);
			drawing(ppos.x, ppos.y, pos.x, pos.y);
			if (_type == 2) {
				while (mouseList.size() > 0) {
					mouseList.remove(0);
					update(movingAverage());
					drawing(ppos.x, ppos.y, pos.x, pos.y);
				}
			}
		}
	}

	@Override
	public void drawing(float _px, float _py, float _x, float _y) {
		pg.beginDraw();
		//		pg.noStroke();
		//		pg.fill(255, 0, 0);
		//		pg.ellipse(_px, _py, 3, 3);
		//		pg.ellipse(_x, _y, 3, 3);
		pg.noFill();
		pg.strokeWeight(1);
		pg.stroke(0, 0, 255);
		pg.line(_px, _py, _x, _y);
		pg.endDraw();
	}
}
