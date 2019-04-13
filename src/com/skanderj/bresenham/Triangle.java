package com.skanderj.bresenham;

import java.awt.Color;

import com.skanderj.bresenham.math.Vertex;

public class Triangle implements Comparable<Triangle> {
	public static final int SIDES = 3;
	
	public Vertex vectors[];
	public Color color;

	public Triangle(Triangle model) {
		this(model.vectors[0].copy(), model.vectors[1].copy(), model.vectors[2].copy(), model.color);
	}

	public Triangle(Vertex firstPoint, Vertex secondPoint, Vertex thirdPoint, Color color) {
		this.vectors = new Vertex[] { firstPoint, secondPoint, thirdPoint };
		this.color = color;
	}

	@Override
	public int compareTo(Triangle foreign) {
		double ownDepth = (this.vectors[0].z + this.vectors[1].z + this.vectors[2].z) / 3.0;
		double foreignDepth = (foreign.vectors[0].z + foreign.vectors[1].z + foreign.vectors[2].z) / 3.0;
		if ((ownDepth - foreignDepth) > 0) {
			return -1;
		} else if ((ownDepth - foreignDepth) < 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return String.format("(%f, %f, %f) (%f, %f, %f) (%f, %f, %f)", this.vectors[0].x, this.vectors[0].y, this.vectors[0].z, this.vectors[1].x, this.vectors[1].y, this.vectors[1].z, this.vectors[2].x, this.vectors[2].y, this.vectors[2].z);
	}
}
