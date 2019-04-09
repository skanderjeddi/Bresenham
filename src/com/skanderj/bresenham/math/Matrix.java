import java.util.Random;

public final class Matrix {
	private final int rows, lines;
	public final double[][] data;

	/**
	 * Generates the n-by-n identity matrix
	 *
	 * @param size
	 * @return [I(n)]
	 */
	public static final Matrix identity(int size) {
		Matrix idmat = new Matrix(size, size);
		for (int cursor = 0; cursor < size; cursor += 1) {
			idmat.data[cursor][cursor] = 1;
		}
		return idmat;
	}

	/**
	 * Generates a random matrix
	 *
	 * @param rows
	 * @param lines
	 * @param magnitude
	 * @return a random matrix filled with positives floating point numbers
	 */
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

	/**
	 * Multiplies the matrix values by the scalar value
	 *
	 * @param scalar
	 * @return k * [M]
	 */
	public static final Matrix multiply(Matrix matrix, double k) {
		Matrix resmat = new Matrix(matrix);
		for (int row = 0; row < matrix.rows; row += 1) {
			for (int line = 0; line < matrix.lines; line += 1) {
				resmat.data[row][line] *= k;
			}
		}
		return resmat;
	}

	/**
	 * Sums current matrix with argument matrix
	 *
	 * @param mat
	 * @return [A] + [B]
	 */
	public static final Matrix add(Matrix firstMat, Matrix secondMatrix) {
		Matrix resmat = new Matrix(firstMat);
		for (int row = 0; row < firstMat.rows; row += 1) {
			for (int line = 0; line < firstMat.lines; line += 1) {
				resmat.data[row][line] += secondMatrix.data[row][line];
			}
		}
		return resmat;
	}

	/**
	 * Subtracts argument matrix from current matrix
	 *
	 * @param mat
	 * @return [A] - [B]
	 */
	public static final Matrix subtract(Matrix firstMatrix, Matrix secondMatrix) {
		return firstMatrix.add(secondMatrix, Matrix.multiply(secondMatrix, -1));
	}

	/**
	 * Mutliplies compatible matrices together
	 *
	 * @param mat
	 * @return [A] * [B]
	 */
	public static final Matrix multiply(Matrix firstMatrix, Matrix secondMatrix) {
		Matrix resmat;
		if (firstMatrix.lines == secondMatrix.rows) {
			resmat = new Matrix(firstMatrix.rows, secondMatrix.lines);
			for (int row = 0; row < resmat.rows; row += 1) {
				for (int line = 0; line < resmat.lines; line += 1) {
					for (int cursor = 0; cursor < firstMatrix.lines; cursor += 1) {
						resmat.data[row][line] += (firstMatrix.data[row][cursor] * secondMatrix.data[cursor][line]);
					}
				}
			}
		} else if (secondMatrix.lines == firstMatrix.rows) {
			resmat = new Matrix(secondMatrix.rows, firstMatrix.lines);
			for (int row = 0; row < resmat.rows; row += 1) {
				for (int line = 0; line < resmat.lines; line += 1) {
					for (int cursor = 0; cursor < secondMatrix.lines; cursor += 1) {
						resmat.data[row][line] += (secondMatrix.data[row][cursor] * firstMatrix.data[cursor][line]);
					}
				}
			}
		} else {
			return null;
		}
		return resmat;
	}

	/**
	 * Generates the transpose of the current matrix
	 *
	 * @return t[A]
	 */
	public static final Matrix transpose(Matrix matrix) {
		Matrix resmat = new Matrix(matrix.lines, matrix.rows);
		for (int row = 0; row < matrix.rows; row += 1) {
			for (int line = 0; line < matrix.lines; line += 1) {
				resmat.data[line][row] = matrix.data[row][line];
			}
		}
		return resmat;
	}

	/**
	 * @return the determinant of a matrix
	 */
	public static final double determinant(Matrix matrix) {
        if (matrix.rows != matrix.lines) {
            throw new IllegalStateException("invalid dimensions");
        }
        if (matrix.rows == 2) {
            return matrix.data[0][0] * matrix.data[1][1] - matrix.data[0][1] * matrix.data[1][0];
        }
        double determinant = 0.0;
        for (int row = 0; row < matrix.rows; row += 1) {
            determinant += Math.pow(-1, row) * matrix.data[0][row] * Matrix.determinant(minor(matrix, 0, row));
        }
        return determinant;
    }

    /**
     * @return the minor of a given matrix
     */
    public static final Matrix minor(Matrix matrix, int targetRow, int targetColumn) {
		Matrix minor = new Matrix(matrix.rows - 1, matrix.lines - 1);
		for (int row = 0; row < matrix.rows; row += 1) {
            for (int line = 0; row != targetRow && line < matrix.lines; line += 1) {
                if (line != targetColumn) {
                    minor.data[row < targetRow ? row : row - 1][line < targetColumn ? line : line - 1] = matrix.data[row][line];
                }
            }
        }
		return minor;
    }
    
    /**
     * @return the inverse of a given matrix
     */
    public static final Matrix inverse(Matrix matrix) {
		Matrix inverse = new Matrix(matrix.rows, matrix.lines);
		for (int row = 0; row < matrix.rows; row += 1) {
            for (int line = 0; line < matrix.lines; line += 1) {
                inverse.data[row][line] = Math.pow(-1, row + line) * determinant(minor(matrix, row, line));
            }
        }
        double determinant = 1.0 / Matrix.determinant(matrix);
		for (int row = 0; row < inverse.rows; row += 1) {
			for (int line = 0; line <= row; line += 1) {
				double temp = inverse.data[row][line];
				inverse.data[row][line] = inverse.data[line][row] * determinant;
				inverse.data[line][row] = temp * determinant;
			}
		}
		return inverse;
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
    
    public static void main(String[] args) {
        Matrix smat = Matrix.random(10, 10, 10);
        Matrix ismat = Matrix.inverse(smat);
        Matrix pmat = Matrix.multiply(smat, ismat);
        smat.print();
        ismat.print();
        pmat.print();
    }
}
