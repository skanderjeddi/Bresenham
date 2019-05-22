package com.skanderj.bresenham.math;

import com.skanderj.bresenham.Bresenham;
import com.skanderj.bresenham.Triangle;

public class Vector4D {
	public double x, y, z, w;

	public Vector4D() {
		this(0.0, 0.0, 0.0);
	}

	public Vector4D(double x, double y, double z) {
		this(x, y, z, 1.0);
	}

	public Vector4D(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vector4D copy() {
		return new Vector4D(this.x, this.y, this.z, this.w);
	}

	/**
	 * Returns the sum of the initial vectors as a new vector
	 */
	public static Vector4D add(Vector4D firstVector, Vector4D secondVector) {
		return new Vector4D(firstVector.x + secondVector.x, firstVector.y + secondVector.y, firstVector.z + secondVector.z);
	}

	/**
	 * Returns the difference of the initial vectors as a new vector
	 */
	public static Vector4D subtract(Vector4D firstVector, Vector4D secondVector) {
		return new Vector4D(firstVector.x - secondVector.x, firstVector.y - secondVector.y, firstVector.z - secondVector.z);
	}

	/**
	 * Returns initial vector scaled up by k-factor as a new vector
	 */
	public static Vector4D multiply(Vector4D vector, double k) {
		return new Vector4D(vector.x * k, vector.y * k, vector.z * k);
	}

	/**
	 * Returns initial vector scaled down by k-factor as a new vector
	 */
	public static Vector4D divide(Vector4D vector, double k) {
		return new Vector4D(vector.x / k, vector.y / k, vector.z / k);
	}

	/**
	 * Returns the dot product of the initial vectors - comparison tool (lighting)
	 */
	public static double dotProduct(Vector4D firstVector, Vector4D secondVector) {
		return (firstVector.x * secondVector.x) + (firstVector.y * secondVector.y) + (firstVector.z * secondVector.z);
	}

	/**
	 * Returns the cross product of the initial vectors as a new vector - useful for
	 * computing normals - result vector will be perpendicular to plane created by
	 * the initial vectors
	 */
	public static Vector4D crossProduct(Vector4D firstVector, Vector4D secondVector) {
		double x = (firstVector.y * secondVector.z) - (firstVector.z * secondVector.y);
		double y = (firstVector.z * secondVector.x) - (firstVector.x * secondVector.z);
		double z = (firstVector.x * secondVector.y) - (firstVector.y * secondVector.x);
		return new Vector4D(x, y, z);
	}

	/**
	 * Returns a normalised version of the initial vector as a new vector
	 */
	public static Vector4D normalize(Vector4D vector) {
		double length = Vector4D.length(vector);
		return new Vector4D(vector.x / length, vector.y / length, vector.z / length);
	}

	/**
	 * Returns the length of the vector
	 */
	public static double length(Vector4D vector) {
		return Math.sqrt((vector.x * vector.x) + (vector.y * vector.y) + (vector.z * vector.z));
	}

	/**
	 * Converts a 1-by-4 matrix to a vector - need to check size
	 */
	public static Vector4D convertMatrixToVector4D(Matrix matrix) {
		double x = matrix.data[0][0];
		double y = matrix.data[0][1];
		double z = matrix.data[0][2];
		double w = matrix.data[0][3];
		return new Vector4D(x, y, z, w);
	}

	/**
	 * Returns the 1-by-4 * 4-by-4 matrices product as a new vector - initial vector
	 * is considered a 1-by-4 matrix - this implementation is messy and could be
	 * refactored to use matrix-matrix multiplication
	 */
	public static Vector4D applyMatrixToVector_PW(Vector4D vector, Matrix matrix) {
		double x = (vector.x * matrix.data[0][0]) + (vector.y * matrix.data[1][0]) + (vector.z * matrix.data[2][0]) + (vector.w * matrix.data[3][0]);
		double y = (vector.x * matrix.data[0][1]) + (vector.y * matrix.data[1][1]) + (vector.z * matrix.data[2][1]) + (vector.w * matrix.data[3][1]);
		double z = (vector.x * matrix.data[0][2]) + (vector.y * matrix.data[1][2]) + (vector.z * matrix.data[2][2]) + (vector.w * matrix.data[3][2]);
		double w = (vector.x * matrix.data[0][3]) + (vector.y * matrix.data[1][3]) + (vector.z * matrix.data[2][3]) + (vector.w * matrix.data[3][3]);
		return new Vector4D(x, y, z, w);
	}

	/**
	 * See note above - refactoring of the function to use matrices product
	 */
	public static Vector4D applyMatrixToVector_MPW(Vector4D vector, Matrix matrix) {
		Matrix vectorMatrix = Matrix.convertVectorToMatrix(vector);
		Matrix productMatrix = Matrix.multiply(vectorMatrix, matrix);
		return Vector4D.convertMatrixToVector4D(productMatrix);
	}

	/**
	 * Scales a vector to viewing distance
	 */
	public static Vector4D scaleVectorToView(Vector4D vector) {
		Vector4D scaledVector = vector.copy();
		scaledVector.x *= Bresenham.HORIZONTAL_SCALING_FACTOR;
		scaledVector.y *= Bresenham.VERTICAL_SCALING_FACTOR;
		return scaledVector;
	}

	/**
	 * Returns the intersection of a plane and a vector
	 */
	public static Vector4D vectorPlaneIntersection(Vector4D planePoint, Vector4D planeNormal, Vector4D lineStart, Vector4D lineEnd) {
		planeNormal = Vector4D.normalize(planeNormal);
		double planeDotProduct = -Vector4D.dotProduct(planeNormal, planePoint);
		double firstEnd = Vector4D.dotProduct(lineStart, planeNormal);
		double secondEnd = Vector4D.dotProduct(lineEnd, planeNormal);
		double tangent = (-planeDotProduct - firstEnd) / (secondEnd - firstEnd);
		Vector4D line = Vector4D.subtract(lineEnd, lineStart);
		Vector4D lineToIntersect = Vector4D.multiply(line, tangent);
		return Vector4D.add(lineStart, lineToIntersect);
	}

	/**
	 * Returns the normal vector to a triangle
	 */
	public static Vector4D normalToTriangle(Triangle triangle) {
		Vector4D firstAxis = Vector4D.subtract(triangle.vectors[1], triangle.vectors[0]);
		Vector4D secondAxis = Vector4D.subtract(triangle.vectors[2], triangle.vectors[0]);
		return Vector4D.crossProduct(firstAxis, secondAxis);
	}

	public void print() {
		System.out.printf("(%f %f %f)\n", this.x, this.y, this.z);
	}
}
