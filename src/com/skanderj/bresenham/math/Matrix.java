package com.skanderj.bresenham.math;

import java.util.Random;

public final class Matrix {
	private final int rows, lines;
	public final double[][] data;

	public static final Matrix identity(int size) {
		Matrix idmat = new Matrix(size, size);
		for (int cursor = 0; cursor < size; cursor += 1) {
			idmat.data[cursor][cursor] = 1;
		}
		return idmat;
	}

	public static final Matrix random(int rows, int lines, int magnitude) {
		Random random = new Random();
		Matrix randommat = new Matrix(rows, lines);
		for (int row = 0; row < rows; row += 1) {
			for (int line = 0; line < lines; line += 1) {
				randommat.data[row][line] = random.nextFloat() * magnitude;
			}
		}
		return randommat;
	}

	public Matrix(int rows, int lines) {
		this.rows = rows;
		this.lines = lines;
		this.data = new double[this.rows][this.lines];
		for (int row = 0; row < this.rows; row += 1) {
			for (int line = 0; line < this.lines; line += 1) {
				this.data[row][line] = 0f;
			}
		}
	}

	public Matrix(double[][] copyContent) {
		this(copyContent.length, copyContent[0].length);
		this.copy(copyContent, this.data);
	}

	public Matrix(Matrix copyMatrix) {
		this(copyMatrix.data);
	}

	public final Matrix scalar(double scalar) {
		Matrix resmat = new Matrix(this);
		for (int row = 0; row < this.rows; row += 1) {
			for (int line = 0; line < this.lines; line += 1) {
				resmat.data[row][line] *= scalar;
			}
		}
		return resmat;
	}

	public final Matrix add(Matrix mat) {
		Matrix resmat = new Matrix(this);
		for (int row = 0; row < this.rows; row += 1) {
			for (int line = 0; line < this.lines; line += 1) {
				resmat.data[row][line] += mat.data[row][line];
			}
		}
		return resmat;
	}

	public final Matrix subtract(Matrix mat) {
		mat.scalar(-1);
		return this.add(mat);
	}

	public final Matrix multiply(Matrix mat) {
		Matrix resmat;
		if (this.lines == mat.rows) {
			resmat = new Matrix(this.rows, mat.lines);
			for (int row = 0; row < resmat.rows; row += 1) {
				for (int line = 0; line < resmat.lines; line += 1) {
					for (int cursor = 0; cursor < this.lines; cursor += 1) {
						resmat.data[row][line] += (this.data[row][cursor] * mat.data[cursor][line]);
					}
				}
			}
		} else if (mat.lines == this.rows) {
			resmat = new Matrix(mat.rows, this.lines);
			for (int row = 0; row < resmat.rows; row += 1) {
				for (int line = 0; line < resmat.lines; line += 1) {
					for (int cursor = 0; cursor < mat.lines; cursor += 1) {
						resmat.data[row][line] += (mat.data[row][cursor] * this.data[cursor][line]);
					}
				}
			}
		} else {
			return null;
		}
		return resmat;
	}

	public final Matrix transpose() {
		Matrix resmat = new Matrix(this.lines, this.rows);
		for (int row = 0; row < this.rows; row += 1) {
			for (int line = 0; line < this.lines; line += 1) {
				resmat.data[line][row] = this.data[row][line];
			}
		}
		return resmat;
	}

	public final void print() {
		for (int row = 0; row < this.rows; row += 1) {
			for (int line = 0; line < this.lines; line += 1) {
				if (line == 0) {
					System.out.print("[\t");
				}
				System.out.printf("%f", this.data[row][line]);
				if (line == (this.lines - 1)) {
					System.out.println("\t]");
				} else {
					System.out.print("\t");
				}
			}
		}
	}

	private final void copy(double[][] source, double[][] target) {
		for (int row = 0; row < source.length; row += 1) {
			for (int line = 0; line < source[row].length; line += 1) {
				target[row][line] = source[row][line];
			}
		}
	}
}
