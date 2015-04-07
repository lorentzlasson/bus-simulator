package net.bluemix.iot.bussimulator.util;

public class Vector {
	public static final Vector Y_UNIT = new Vector(0, 1);
	public static final Vector X_UNIT = new Vector(1, 0);
	private double x;
	private double y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector(Vector v) {
		x = v.x;
		y = v.y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}

	public Vector add(Vector v) {
		double x = this.x + v.x;
		double y = this.y + v.y;
		return new Vector(x, y);
	}

	public Vector subtract(Vector v) {
		return add(v.multiply(-1));
	}
	
	public Vector multiply(double factor) {
		double x = this.x * factor;
		double y = this.y * factor;
		return new Vector(x, y);
	}
	
	public Vector negative() {
		return multiply(-1);
	}

	public double size() {
		return (double) Math.pow(Math.pow(x, 2) + Math.pow(y, 2), 0.5);
	}

	public Vector normalized() {
		double size = size();
		x /= size;
		y /= size;
		return this;
	}	
	
	public double dotProduct(Vector v) {
		return x*v.x + y*v.y;
	}
	
	public Vector bounceVerticallSurface(double energyLossFactor) {
		x = (double) (-x*energyLossFactor);
		return this; 
	}
	
	public Vector bounceHorizontalSurface(double energyLossFactor) {
		if(Math.abs(y) > 0.01) {
			y = (double) (-y*energyLossFactor);
		} else {
			y = 0;
		}
		return this;
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	public Vector clone() {
		return new Vector(x, y);
	}
	
	public void update(Vector v) {
		x = v.x;
		y = v.y;
	}
}
