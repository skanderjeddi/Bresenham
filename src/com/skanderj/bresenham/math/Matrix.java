package com.skanderj.bresenham.math;

import java.util.Random;

import com.skanderj.bresenham.Triangle;

public final class Matrix {
	private final int rows, lines;
	public final double[][] data;

	public static final Matrix identity(int size) {
		Matrix identityMatrix = new Matrix(size, size);
		for (int cursor = 0; cursor < size; cursor += 1) {
			identityMatrix.data[cursor][cursor] = 1;
		}
		return identityMatrix;
	}

	public static final Matrix random(int rows, int lines, int magnitude) {
		Random random = new Random();
		Matrix randomMatrix = new Matrix(rows, lines);
		for (int row = 0; row < rows; row += 1) {
			for (int line = 0; line < lines; line += 1) {
				randomMatrix.data[row][line] = random.nextFloat() * magnitude;
			}
		}
		return randomMatrix;
	}

	public static final boolean isSquare(Matrix matrix) {
		return matrix.rows == matrix.lines;
	}

	public static final boolean isDiagonal(Matrix matrix) {
		boolean square = Matrix.isSquare(matrix);
		if (square) {
			for (int row = 0; row < matrix.rows; row += 1) {
				for (int line = 0; line < matrix.lines; line += 1) {
					if (row == line) {
						continue;
					} else {
						if (matrix.data[row][line] == 0) {
							continue;
						} else {
							return false;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public static final boolean isLowerTriangular(Matrix matrix) {
		boolean square = Matrix.isSquare(matrix);
		if (square) {
			for (int row = 0; row < matrix.rows; row += 1) {
				for (int line = 0; line < matrix.lines; line += 1) {
					if (row == line) {
						continue;
					} else {
						if (row > line) {
							if (matrix.data[row][line] == 0) {
								continue;
							} else {
								return false;
							}
						} else {
							continue;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public static final boolean isUpperTriangular(Matrix matrix) {
		boolean square = Matrix.isSquare(matrix);
		if (square) {
			for (int row = 0; row < matrix.rows; row += 1) {
				for (int line = 0; line < matrix.lines; line += 1) {
					if (row == line) {
						continue;
					} else {
						if (row < line) {
							if (matrix.data[row][line] == 0) {
								continue;
							} else {
								return false;
							}
						} else {
							continue;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public Matrix(int rows, int lines) {
		this.rows = rows;
		this.lines = lines;
		this.data = new double[this.rows][this.lines];
		for (int row = 0; row < this.rows; row += 1) {
			for (int line = 0; line < this.lines; line += 1) {
				this.data[row][line] = 0;
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

	public static final Matrix scale(Matrix matrix, double k) {
		Matrix resultMatrix = new Matrix(matrix);
		for (int row = 0; row < matrix.rows; row += 1) {
			for (int line = 0; line < matrix.lines; line += 1) {
				resultMatrix.data[row][line] *= k;
			}
		}
		return resultMatrix;
	}

	public static final Matrix sum(Matrix firstMat, Matrix secondMatrix) {
		Matrix resultMatrix = new Matrix(firstMat);
		for (int row = 0; row < firstMat.rows; row += 1) {
			for (int line = 0; line < firstMat.lines; line += 1) {
				resultMatrix.data[row][line] += secondMatrix.data[row][line];
			}
		}
		return resultMatrix;
	}

	public static final Matrix difference(Matrix firstMat, Matrix secondMatrix) {
		Matrix resultMatrix = new Matrix(firstMat);
		for (int row = 0; row < firstMat.rows; row += 1) {
			for (int line = 0; line < firstMat.lines; line += 1) {
				resultMatrix.data[row][line] -= secondMatrix.data[row][line];
			}
		}
		return resultMatrix;
	}

	public static final Matrix product(Matrix firstMatrix, Matrix secondMatrix) {
		Matrix resultMatrix;
		if (firstMatrix.lines == secondMatrix.rows) {
			resultMatrix = new Matrix(firstMatrix.rows, secondMatrix.lines);
			for (int row = 0; row < resultMatrix.rows; row += 1) {
				for (int line = 0; line < resultMatrix.lines; line += 1) {
					for (int cursor = 0; cursor < firstMatrix.lines; cursor += 1) {
						resultMatrix.data[row][line] += (firstMatrix.data[row][cursor] * secondMatrix.data[cursor][line]);
					}
				}
			}
		} else if (secondMatrix.lines == firstMatrix.rows) {
			resultMatrix = new Matrix(secondMatrix.rows, firstMatrix.lines);
			for (int row = 0; row < resultMatrix.rows; row += 1) {
				for (int line = 0; line < resultMatrix.lines; line += 1) {
					for (int cursor = 0; cursor < secondMatrix.lines; cursor += 1) {
						resultMatrix.data[row][line] += (secondMatrix.data[row][cursor] * firstMatrix.data[cursor][line]);
					}
				}
			}
		} else {
			return null;
		}
		return resultMatrix;
	}

	public static final Matrix transpose(Matrix matrix) {
		Matrix resultMatrix = new Matrix(matrix.lines, matrix.rows);
		for (int row = 0; row < matrix.rows; row += 1) {
			for (int line = 0; line < matrix.lines; line += 1) {
				resultMatrix.data[line][row] = matrix.data[row][line];
			}
		}
		return resultMatrix;
	}

	public static final double determinant(Matrix matrix) {
		if (matrix.rows != matrix.lines) {
			throw new IllegalStateException("Invalid dimensions");
		}
		if (matrix.rows == 2) {
			return (matrix.data[0][0] * matrix.data[1][1]) - (matrix.data[0][1] * matrix.data[1][0]);
		}
		double determinant = 0.0;
		for (int row = 0; row < matrix.rows; row += 1) {
			determinant += Math.pow(-1, row) * matrix.data[0][row] * Matrix.determinant(Matrix.minor(matrix, 0, row));
		}
		return determinant;
	}

	public static final double diagonalProduct(Matrix matrix) {
		boolean square = Matrix.isSquare(matrix);
		if (square) {
			double diagonalProduct = 1.0;
			for (int slot = 0; slot < matrix.lines; slot += 1) {
				diagonalProduct *= matrix.data[slot][slot];
			}
			return diagonalProduct;
		} else {
			throw new IllegalStateException("Invalid dimensions");
		}
	}

	public static final Matrix minor(Matrix matrix, int targetRow, int targetColumn) {
		Matrix minor = new Matrix(matrix.rows - 1, matrix.lines - 1);
		for (int row = 0; row < matrix.rows; row += 1) {
			for (int line = 0; (row != targetRow) && (line < matrix.lines); line += 1) {
				if (line != targetColumn) {
					minor.data[row < targetRow ? row : row - 1][line < targetColumn ? line : line - 1] = matrix.data[row][line];
				}
			}
		}
		return minor;
	}

	public static final Matrix inverse(Matrix matrix) {
		Matrix inverse = new Matrix(matrix.rows, matrix.lines);
		for (int row = 0; row < matrix.rows; row += 1) {
			for (int line = 0; line < matrix.lines; line += 1) {
				inverse.data[row][line] = Math.pow(-1, row + line) * Matrix.determinant(Matrix.minor(matrix, row, line));
			}
		}
		double determinant = 1.0 / Matrix.determinant(matrix);
		for (int row = 0; row < inverse.rows; row += 1) {
			for (int line = 0; line <= row; line += 1) {
				double storedValue = inverse.data[row][line];
				inverse.data[row][line] = inverse.data[line][row] * determinant;
				inverse.data[line][row] = storedValue * determinant;
			}
		}
		return inverse;
	}

	/**
	 * Converts a vertex to a 1-by-4 matrix
	 */
	public static Matrix convertVectorToMatrix(Vector4D vector) {
		Matrix vectMat = new Matrix(1, 4);
		vectMat.data[0][0] = vector.x;
		vectMat.data[0][1] = vector.y;
		vectMat.data[0][2] = vector.z;
		vectMat.data[0][3] = vector.w;
		return vectMat;
	}

	/**
	 * Converts a triangle to a 3-by-4 matrix
	 */
	public static Matrix convertTriangleToMatrix(Triangle triangle) {
		Matrix triangleMat = new Matrix(3, 4);
		for (int index = 0; index < Triangle.SIDES; index += 1) {
			triangleMat.data[index] = Matrix.convertVectorToMatrix(triangle.vectors[index]).data[0];
		}
		return triangleMat;
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
