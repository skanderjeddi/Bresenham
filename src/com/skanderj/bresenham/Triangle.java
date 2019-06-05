package com.skanderj.bresenham;

import java.awt.Color;

import com.skanderj.bresenham.math.Matrix;
import com.skanderj.bresenham.math.Vector4D;

public class Triangle implements Comparable<Triangle> {
	public static final int SIDES = 3;

	public Vector4D vectors[];
	public Color color;

	public Triangle(Triangle model) {
		this(model.vectors[0].copy(), model.vectors[1].copy(), model.vectors[2].copy(), model.color);
	}

	public Triangle(Vector4D firstPoint, Vector4D secondPoint, Vector4D thirdPoint, Color color) {
		this.vectors = new Vector4D[] { firstPoint, secondPoint, thirdPoint };
		this.color = color;
	}

	/**
	 * Converts a 3-by-4 matrix to a triangle - need to check size
	 */
	public static Triangle convertMatrixToTriangle(Matrix matrix, Color color) {
		Vector4D firstVector = new Vector4D(matrix.data[0][0], matrix.data[0][1], matrix.data[0][2], matrix.data[0][3]);
		Vector4D secondVector = new Vector4D(matrix.data[1][0], matrix.data[1][1], matrix.data[1][2], matrix.data[1][3]);
		Vector4D thirdVector = new Vector4D(matrix.data[2][0], matrix.data[2][1], matrix.data[2][2], matrix.data[2][3]);
		return new Triangle(firstVector, secondVector, thirdVector, color);
	}

	/**
	 * Returns the transformed-by-matrix triangle - this implementation is messy and
	 * could be refactored to use matrix-matrix multiplication
	 */
	public static Triangle applyMatrixToTriangle_OW(Triangle triangle, Matrix matrix) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vectors[index] = Vector4D.applyMatrixToVector_MPW(transformedTriangle.vectors[index], matrix);
		}
		return transformedTriangle;
	}

	/**
	 * See note above - refactoring of the function to use matrices product
	 */
	public static Triangle applyMatrixToTriangle_NW(Triangle triangle, Matrix matrix) {
		Matrix triangleMatrix = Matrix.convertTriangleToMatrix(triangle);
		Matrix resultMatrix = Matrix.product(triangleMatrix, matrix);
		return Triangle.convertMatrixToTriangle(resultMatrix, triangle.color);
	}

	/**
	 * Returns a new triangle - sum of the initial triangle's vectors coordinates &
	 * the initial vector - this implementation is messy and could be refactored
	 */
	public static Triangle addVectorToTriangle(Triangle triangle, Vector4D vector) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vectors[index] = Vector4D.add(transformedTriangle.vectors[index], vector);
		}
		return transformedTriangle;
	}

	/**
	 * Returns a new triangle - difference of the initial triangle's vectors
	 * coordinates & the initial vector - this implementation is messy and could be
	 * refactored
	 */
	public static Triangle subtractVectorFromTriangle(Triangle triangle, Vector4D vector) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vectors[index] = Vector4D.subtract(transformedTriangle.vectors[index], vector);
		}
		return transformedTriangle;
	}

	/**
	 * Returns a new triangle - normalises all the vectors of the initial triangle
	 */
	public static Triangle normalizeTriangle(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vectors[index] = Vector4D.divide(transformedTriangle.vectors[index], transformedTriangle.vectors[index].w);
		}
		return transformedTriangle;
	}

	/**
	 * Scales a triangle to viewing distance
	 */
	public static Triangle scaleTriangleToView(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			transformedTriangle.vectors[index] = Vector4D.scaleVectorToView(transformedTriangle.vectors[index]);
		}
		return transformedTriangle;
	}

	/**
	 * Flips 2d coords
	 */
	public static Triangle flipXYCoordinates(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		transformedTriangle.vectors[0].x *= -1.0f;
		transformedTriangle.vectors[1].x *= -1.0f;
		transformedTriangle.vectors[2].x *= -1.0f;
		transformedTriangle.vectors[0].y *= -1.0f;
		transformedTriangle.vectors[1].y *= -1.0f;
		transformedTriangle.vectors[2].y *= -1.0f;
		return transformedTriangle;
	}

	public void copy(Triangle triangle) {
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			this.vectors[index] = triangle.vectors[index];
		}
		this.color = triangle.color;
	}

	@Override
	public int compareTo(Triangle foreign) {
		double ownDepth = (this.vectors[0].z + this.vectors[1].z + this.vectors[2].z) / 3.0;
		double foreignDepth = (foreign.vectors[0].z + foreign.vectors[1].z + foreign.vectors[2].z) / 3.0;
		return ownDepth > foreignDepth ? -1 : ownDepth == foreignDepth ? 0 : 1;
	}

	@Override
	public String toString() {
		return String.format("(%f %f %f) (%f %f %f) (%f %f %f)", this.vectors[0].x, this.vectors[0].y, this.vectors[0].z, this.vectors[1].x, this.vectors[1].y, this.vectors[1].z, this.vectors[2].x, this.vectors[2].y, this.vectors[2].z);
	}
}
