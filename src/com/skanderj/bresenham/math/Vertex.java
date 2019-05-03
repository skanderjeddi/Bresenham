package com.skanderj.bresenham.math;

import com.skanderj.bresenham.Bresenham;
import com.skanderj.bresenham.Triangle;

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

	/**
	 * Returns the sum of the initial vertices as a new vertex
	 */
	public static Vertex add(Vertex firstVertex, Vertex secondVertex) {
		return new Vertex(firstVertex.x + secondVertex.x, firstVertex.y + secondVertex.y, firstVertex.z + secondVertex.z);
	}

	/**
	 * Returns the difference of the initial vertices as a new vertex
	 */
	public static Vertex subtract(Vertex firstVertex, Vertex secondVertex) {
		return new Vertex(firstVertex.x - secondVertex.x, firstVertex.y - secondVertex.y, firstVertex.z - secondVertex.z);
	}

	/**
	 * Returns initial vertex scaled up by k-factor as a new vertex
	 */
	public static Vertex multiply(Vertex vertex, double k) {
		return new Vertex(vertex.x * k, vertex.y * k, vertex.z * k);
	}

	/**
	 * Returns initial vertex scaled down by k-factor as a new vertex
	 */
	public static Vertex divide(Vertex vertex, double k) {
		return new Vertex(vertex.x / k, vertex.y / k, vertex.z / k);
	}

	/**
	 * Returns the dot product of the initial vertices - comparison tool (lighting)
	 */
	public static double dotProduct(Vertex firstVertex, Vertex secondVertex) {
		return (firstVertex.x * secondVertex.x) + (firstVertex.y * secondVertex.y) + (firstVertex.z * secondVertex.z);
	}

	/**
	 * Returns the cross product of the initial vertices as a new vertex - useful
	 * for computing normals - result vertex will be perpendicular to plane created
	 * by the initial vertices
	 */
	public static Vertex crossProduct(Vertex firstVertex, Vertex secondVertex) {
		double x = (firstVertex.y * secondVertex.z) - (firstVertex.z * secondVertex.y);
		double y = (firstVertex.z * secondVertex.x) - (firstVertex.x * secondVertex.z);
		double z = (firstVertex.x * secondVertex.y) - (firstVertex.y * secondVertex.x);
		return new Vertex(x, y, z);
	}

	/**
	 * Returns a normalised version of the initial vertex as a new vertex
	 */
	public static Vertex normalize(Vertex vertex) {
		double length = Vertex.length(vertex);
		return new Vertex(vertex.x / length, vertex.y / length, vertex.z / length);
	}

	/**
	 * Returns the length of the vertex
	 */
	public static double length(Vertex vertex) {
		return Math.sqrt((vertex.x * vertex.x) + (vertex.y * vertex.y) + (vertex.z * vertex.z));
	}

	/**
	 * Converts a 1-by-4 matrix to a vertex - need to check size
	 */
	public static Vertex convertMatrixToVertex(Matrix matrix) {
		double x = matrix.data[0][0];
		double y = matrix.data[0][1];
		double z = matrix.data[0][2];
		double w = matrix.data[0][3];
		return new Vertex(x, y, z, w);
	}

	/**
	 * Returns the 1-by-4 * 4-by-4 matrices product as a new vertex - initial vertex
	 * is considered a 1-by-4 matrix - this implementation is messy and could be
	 * refactored to use matrix-matrix multiplication
	 */
	public static Vertex applyMatrixToVector_PW(Vertex vertex, Matrix matrix) {
		double x = (vertex.x * matrix.data[0][0]) + (vertex.y * matrix.data[1][0]) + (vertex.z * matrix.data[2][0]) + (vertex.w * matrix.data[3][0]);
		double y = (vertex.x * matrix.data[0][1]) + (vertex.y * matrix.data[1][1]) + (vertex.z * matrix.data[2][1]) + (vertex.w * matrix.data[3][1]);
		double z = (vertex.x * matrix.data[0][2]) + (vertex.y * matrix.data[1][2]) + (vertex.z * matrix.data[2][2]) + (vertex.w * matrix.data[3][2]);
		double w = (vertex.x * matrix.data[0][3]) + (vertex.y * matrix.data[1][3]) + (vertex.z * matrix.data[2][3]) + (vertex.w * matrix.data[3][3]);
		return new Vertex(x, y, z, w);
	}

	/**
	 * See note above - refactoring of the function to use matrices product
	 */
	public static Vertex applyMatrixToVector_MPW(Vertex vertex, Matrix matrix) {
		Matrix vectMat = Matrix.convertVertexToMatrix(vertex);
		Matrix productMat = Matrix.multiply(vectMat, matrix);
		return Vertex.convertMatrixToVertex(productMat);
	}

	/**
	 * Scales a vertex to viewing distance
	 */
	public static void scaleVertexToView(Vertex vertex) {
		vertex.x *= Bresenham.HORIZONTAL_SCALING_FACTOR;
		vertex.y *= Bresenham.VERTICAL_SCALING_FACTOR;
	}

	/**
	 * Returns the intersection of a plane and a vertex
	 */
	public static Vertex vertexPlaneIntersection(Vertex planePoint, Vertex planeNormal, Vertex lineStart, Vertex lineEnd) {
		planeNormal = Vertex.normalize(planeNormal);
		double planeDotProduct = -Vertex.dotProduct(planeNormal, planePoint);
		double firstEnd = Vertex.dotProduct(lineStart, planeNormal);
		double secondEnd = Vertex.dotProduct(lineEnd, planeNormal);
		double tangent = (-planeDotProduct - firstEnd) / (secondEnd - firstEnd);
		Vertex line = Vertex.subtract(lineEnd, lineStart);
		Vertex lineToIntersect = Vertex.multiply(line, tangent);
		return Vertex.add(lineStart, lineToIntersect);
	}

	/**
	 * Returns the normal vertex to a triangle
	 */
	public static Vertex normalToTriangle(Triangle triangle) {
		Vertex firstAxis = Vertex.subtract(triangle.vertices[1], triangle.vertices[0]);
		Vertex secondAxis = Vertex.subtract(triangle.vertices[2], triangle.vertices[0]);
		return Vertex.crossProduct(firstAxis, secondAxis);
	}

	public void print() {
		System.out.printf("(%f, %f, %f)\n", this.x, this.y, this.z);
	}
}
