package com.skanderj.bresenham;

import java.awt.Color;

import com.skanderj.bresenham.math.Matrix;
import com.skanderj.bresenham.math.Vertex;

public class Triangle implements Comparable<Triangle> {
	public static final int SIDES = 3;

	public Vertex vertices[];
	public Color color;

	public Triangle(Triangle model) {
		this(model.vertices[0].copy(), model.vertices[1].copy(), model.vertices[2].copy(), model.color);
	}

	public Triangle(Vertex firstPoint, Vertex secondPoint, Vertex thirdPoint, Color color) {
		this.vertices = new Vertex[] { firstPoint, secondPoint, thirdPoint };
		this.color = color;
	}

	/**
	 * Converts a 3-by-4 matrix to a triangle - need to check size
	 */
	public static Triangle convertMatrixToTriangle(Matrix matrix, Color color) {
		Vertex firstVertex = new Vertex(matrix.data[0][0], matrix.data[0][1], matrix.data[0][2], matrix.data[0][3]);
		Vertex secondVertex = new Vertex(matrix.data[1][0], matrix.data[1][1], matrix.data[1][2], matrix.data[1][3]);
		Vertex thirdVertex = new Vertex(matrix.data[2][0], matrix.data[2][1], matrix.data[2][2], matrix.data[2][3]);
		return new Triangle(firstVertex, secondVertex, thirdVertex, color);
	}

	/**
	 * Returns the transformed-by-matrix triangle - this implementation is messy and
	 * could be refactored to use matrix-matrix multiplication
	 */
	public static Triangle applyMatrixToTriangle_OW(Triangle triangle, Matrix matrix) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vertices[index] = Vertex.applyMatrixToVector_MPW(transformedTriangle.vertices[index], matrix);
		}
		return transformedTriangle;
	}

	/**
	 * See note above - refactoring of the function to use matrices product
	 */
	public static Triangle applyMatrixToTriangle_NW(Triangle triangle, Matrix matrix) {
		Matrix triangleMatrix = Matrix.convertTriangleToMatrix(triangle);
		Matrix resultMatrix = Matrix.multiply(triangleMatrix, matrix);
		return Triangle.convertMatrixToTriangle(resultMatrix, triangle.color);
	}

	/**
	 * Returns a new triangle - sum of the initial triangle's vertices coordinates &
	 * the initial vertex - this implementation is messy and could be refactored
	 */
	public static Triangle addVertexToTriangle(Triangle triangle, Vertex vertex) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vertices[index] = Vertex.add(transformedTriangle.vertices[index], vertex);
		}
		return transformedTriangle;
	}

	/**
	 * Returns a new triangle - difference of the initial triangle's vertices
	 * coordinates & the initial vertex - this implementation is messy and could be
	 * refactored
	 */
	public static Triangle subtractVertexFromTriangle(Triangle triangle, Vertex vertex) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vertices[index] = Vertex.subtract(transformedTriangle.vertices[index], vertex);
		}
		return transformedTriangle;
	}

	/**
	 * Returns a new triangle - normalises all the vectors of the initial triangle
	 */
	public static Triangle normalizeTriangle(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vertices[index] = Vertex.divide(transformedTriangle.vertices[index], transformedTriangle.vertices[index].w);
		}
		return transformedTriangle;
	}

	/**
	 * Scales a triangle to viewing distance
	 */
	public static Triangle scaleTriangleToView(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			Vertex.scaleVertexToView(transformedTriangle.vertices[index]);
		}
		return transformedTriangle;
	}

	@Override
	public int compareTo(Triangle foreign) {
		double ownDepth = (this.vertices[0].z + this.vertices[1].z + this.vertices[2].z) / 3.0;
		double foreignDepth = (foreign.vertices[0].z + foreign.vertices[1].z + foreign.vertices[2].z) / 3.0;
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
		return String.format("(%f, %f, %f) (%f, %f, %f) (%f, %f, %f)", this.vertices[0].x, this.vertices[0].y, this.vertices[0].z, this.vertices[1].x, this.vertices[1].y, this.vertices[1].z, this.vertices[2].x, this.vertices[2].y, this.vertices[2].z);
	}
}
