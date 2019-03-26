package com.skanderj.bresenham.math;

public class Vertex {
	public double x, y, z, w;

	public Vertex() {
		this(0.0, 0.0, 0.0);
	}

	public Vertex(double x, double y, double z) {
		this(x, y, z, 1.0);
	}

	public Vertex(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vertex copy() {
		return new Vertex(this.x, this.y, this.z, this.w);
	}
	public void print() {
		System.out.printf("(%f, %f, %f)\n", this.x, this.y, this.z);
	}
}
