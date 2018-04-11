package main;

import main.TabletInput.ReceivingPoint;
import processing.core.PApplet;
import processing.core.PVector;

public class MassMethod extends Drawing implements ReceivingPoint {
	private PVector	mouse		= new PVector();
	private PVector	pmouse	= new PVector();
	private PVector	pos			= new PVector();
	private PVector	ppos		= new PVector();
	private PVector	vel			= new PVector();
	private PVector	acc			= new PVector();
	private float		mass		= 1;
	private float		damping	= 0.5f;

	public MassMethod(PApplet _p5, int w, int h) {
		super(_p5, w, h);
	}

	private PVector force() {
		return PVector.sub(mouse, pmouse);
	}

	private void update(PVector _force) {
		ppos.set(pos);
		acc.set(_force);
		acc.div(mass);
		vel.add(acc);
		pos.add(vel);
		vel.mult(damping);
	}

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public float getDamping() {
		return damping;
	}

	public void setDamping(float damping) {
		this.damping = damping;
	}

	@Override
	public void receivePoint(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure,
			float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
		if (_nthPointInStroke == 1) {
			pmouse.set(_x, _y);
			pos.set(_x, _y);
			acc.set(0, 0);
			vel.set(0, 0);
		}
		else {
			mouse.set(_x, _y);
			update(force());
			pmouse.set(mouse);
			drawing(ppos.x, ppos.y, pos.x, pos.y);
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
