package com.skanderj.bresenham;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import com.skanderj.bresenham.math.Matrix;
import com.skanderj.bresenham.math.Triangle;
import com.skanderj.bresenham.math.Vertex;
import com.skanderj.gingerbread.Process;
import com.skanderj.gingerbread.core.Game;
import com.skanderj.gingerbread.display.Window;
import com.skanderj.gingerbread.input.Keyboard;
import com.skanderj.gingerbread.input.Mouse;

public class Bresenham extends Game {
	public static final String IDENTIFIER = "Bresenham";
	public static final double RATE = 60.0D;

	public static final double WIDTH = 250, HEIGHT = 200;
	public static final double WIDTH_SCALING_FACTOR = 0.5 * Bresenham.EFFECTIVE_WIDTH, HEIGHT_SCALING_FACTOR = 0.5 * Bresenham.EFFECTIVE_HEIGHT;

	public static final int OG = 0, SCALE = 4, BUFFERS = 2, EFFECTIVE_WIDTH = (int) (Bresenham.WIDTH * Bresenham.SCALE), EFFECTIVE_HEIGHT = (int) (Bresenham.HEIGHT * Bresenham.SCALE);
	public static final double FOV_DEGREES = 90, ASPECT_RATIO = Bresenham.HEIGHT / Bresenham.WIDTH, NEAR_FIELD = 0.1, FAR_FIELD = 1000.0;

	private Window window;
	private Keyboard keyboard;
	private Mouse mouse;

	private Matrix projectionMatrix, translationMatrix, zRotationMatrix, xRotationMatrix, worldMatrix, cameraMatrix, viewMatrix;
	private double rotationAngle;

	private Vertex cameraLocation, lightDirection, upAxis, gaze, target;
	private double yaw;

	private Mesh mainMesh;
	private String meshFile;

	public Bresenham() {
		super(Bresenham.IDENTIFIER, Bresenham.RATE);
		this.window = new Window(this, Bresenham.IDENTIFIER, Bresenham.EFFECTIVE_WIDTH, Bresenham.EFFECTIVE_HEIGHT);
		this.keyboard = new Keyboard();
		this.mouse = new Mouse();
		this.meshFile = "teapot.obj";
	}

	@Override
	protected void create() {
		// Initialize camera location & lighting
		{
			// Camera location as vertex - will change in the future for real camera
			// implementation
			this.cameraLocation = new Vertex(0.0, 0.0, 0.0);
			// Light direction as negative z axis - "coming towards the player" to allow
			// lighting
			this.lightDirection = new Vertex(0.0, 1.0, -1.0);
			// Normalize light vertex
			this.lightDirection = this.normalize(this.lightDirection);
			// Up vertex
			this.upAxis = new Vertex(0.0, 1.0, 0.0, 1.0);
			// Gaze
			this.gaze = new Vertex(0.0, 0.0, 1.0);
			// Target
			this.target = new Vertex(0.0, 0.0, 1.0);
			// Yaw
			this.yaw = 0;
		}
		// Initialize program matrices
		{
			// Projection matrix
			this.projectionMatrix = this.createProjectionMatrix(Bresenham.FOV_DEGREES, Bresenham.ASPECT_RATIO, Bresenham.NEAR_FIELD, Bresenham.FAR_FIELD);
			// Translation matrix
			this.translationMatrix = this.createTranslationMatrix(0.0, 0.0, 5.0);
			// Initialize rotation angle
			this.rotationAngle = 0.0;
			// Create z axis rotation matrix
			this.zRotationMatrix = this.createZRotationMatrix(this.rotationAngle);
			// Create x axis rotation matrix
			this.xRotationMatrix = this.createXRotationMatrix(this.rotationAngle);
			// Create world-view matrix
			this.worldMatrix = Matrix.identity(4);
			// Create camera matrices
			this.cameraMatrix = new Matrix(4, 4);
			this.viewMatrix = new Matrix(4, 4);
		}
		// Spaceship mesh
		try {
			this.mainMesh = Mesh.loadFromFile(this.meshFile);
		} catch (NumberFormatException | IOException exception) {
			exception.printStackTrace();
			// Can't load mesh so exit
			System.exit(Process.EXIT_FAILURE);
		}
		// Show window
		this.window.registerKeyboard(this.keyboard);
		this.window.registerMouse(this.mouse);
		this.window.show();
	}

	@Override
	protected void destroy() {
		this.window.hide();
		System.exit(Process.EXIT_SUCCESS);
	}

	@Override
	public void update(double delta) {
		boolean isCloseRequested = this.window.isCloseRequested();
		if (isCloseRequested) {
			this.stop();
		}
		// Handle logic here
		{
			// Keyboard checks
			boolean upKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_UP);
			boolean downKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_DOWN);
			boolean rightKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_RIGHT);
			boolean leftKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_LEFT);
			boolean qKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_Q);
			boolean dKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_D);
			boolean zKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_Z);
			boolean sKeyHeld = this.keyboard.isKeyDown(KeyEvent.VK_S);
			// Move camera
			{
				if (upKeyHeld) {
					this.cameraLocation.y += 0.5 * delta;
				}
				if (downKeyHeld) {
					this.cameraLocation.y -= 0.5 * delta;
				}
				if (rightKeyHeld) {
					this.cameraLocation.x -= 0.5 * delta;
				}
				if (leftKeyHeld) {
					this.cameraLocation.x += 0.5 * delta;
				}
			}
			Vertex scaledDirection = this.multiply(this.gaze, 0.5 * delta);
			// Handle camera rotation
			{
				// Turn left
				if (qKeyHeld) {
					this.yaw -= 0.05 * delta;
				}
				// Turn right
				if (dKeyHeld) {
					this.yaw += 0.05 * delta;
				}
				// Go forward
				if (zKeyHeld) {
					this.cameraLocation = this.subtract(this.cameraLocation, scaledDirection);
				}
				// Go backwards
				if (sKeyHeld) {
					this.cameraLocation = this.add(this.cameraLocation, scaledDirection);
				}
			}
		}
		// Update rotation angle (optional)
		this.rotationAngle += 0.05 * delta;
		// Update rotation matrices
		{
			// Update z rotation matrix
			this.updateZRotationMatrix(this.zRotationMatrix, this.rotationAngle * 0.5);
			// Update x rotation matrix
			this.updateXRotationMatrix(this.xRotationMatrix, this.rotationAngle);
			// Update y rotation matrix (optional)
			// this.updateYRotationMatrix(this.yRotationMatrix, this.rotationAngle);
		}
		// Create transformation matrix
		{
			// Z * X rotation product
			this.worldMatrix = this.zRotationMatrix.multiply(this.xRotationMatrix);
			// Translation
			this.worldMatrix = this.worldMatrix.multiply(this.translationMatrix);
		}
		// Camera handling
		{
			this.target = new Vertex(0.0, 0.0, 1.0);
			Matrix cameraRotationMatrix = this.createYRotationMatrix(this.yaw);
			this.gaze = this.applyMatrixToVector_MPW(this.target, cameraRotationMatrix);
			this.target = this.add(this.cameraLocation, this.gaze);
			this.cameraMatrix = this.pointAt(this.cameraLocation, this.target, this.upAxis);
			this.viewMatrix = this.quickInverse(this.cameraMatrix);
		}
		// Update keyboard & mouse
		{
			this.keyboard.update();
			this.mouse.update();
		}
	}

	@Override
	public void render() {
		BufferStrategy bufferStrategy = this.window.getBufferStrategy(Bresenham.BUFFERS);
		Graphics graphics = bufferStrategy.getDrawGraphics();
		{
			// Set worse rendering hints (possibly optional?)
			this.decreaseRenderQuality(graphics);
			// Clear the screen
			graphics.setColor(Color.BLACK);
			graphics.fillRect(Bresenham.OG, Bresenham.OG, Bresenham.EFFECTIVE_WIDTH, Bresenham.EFFECTIVE_HEIGHT);
			// Triangles transform
			Vector<Triangle> queueVector = new Vector<Triangle>();
			{
				// Parse triangles in cube mesh
				for (Triangle triangle : this.mainMesh.triangles) {
					// Copy data from current triangle - we don't want to change the original data!
					Triangle localTriangle = new Triangle(triangle);
					// Transform localTriangle
					localTriangle = this.applyMatrixToTriangle_NW(localTriangle, this.worldMatrix);
					// Calculate normal data
					Vertex normalVertex = this.normalToTriangle(localTriangle);
					normalVertex = this.normalize(normalVertex);
					// Calculate camera ray
					Vertex cameraRay = this.subtract(localTriangle.vectors[0], this.cameraLocation);
					// Calculate dot product to evaluate if triangle is in view
					double normalCameraDotProduct = this.dotProduct(normalVertex, cameraRay);
					if (normalCameraDotProduct < 0.0) {
						// Set color
						float dotProduct = (float) Math.max(0.1f, this.dotProduct(this.lightDirection, normalVertex));
						localTriangle.color = new Color(dotProduct, dotProduct, dotProduct);
						// Transform world space to view space
						localTriangle = this.applyMatrixToTriangle_NW(localTriangle, this.viewMatrix);
						// Multiply by protection matrix 3D -> 2D
						localTriangle = this.applyMatrixToTriangle_NW(localTriangle, this.projectionMatrix);
						// Normalize
						localTriangle = this.normalizeTriangle(localTriangle);
						// Scale into view
						Vertex viewOffset = new Vertex(1.0, 1.0, 0.0);
						localTriangle = this.addVertexToTriangle(localTriangle, viewOffset);
						localTriangle = this.scaleTriangleToView(localTriangle);
						// Add to vector
						queueVector.add(localTriangle);
					}
				}
			}
			// Sort all the vertices
			Collections.sort(queueVector);
			// Draw sorted triangles
			{
				for (Triangle orderedTriangle : queueVector) {
					graphics.setColor(orderedTriangle.color);
					// Own algorithm, neither fast nor really slow
					/**
					 * this.drawTriangle_LINE_BY_LINE(graphics, orderedTriangle.vectors[0].x,
					 * orderedTriangle.vectors[0].y, // \n orderedTriangle.vectors[1].x,
					 * orderedTriangle.vectors[1].y, // \n orderedTriangle.vectors[2].x,
					 * orderedTriangle.vectors[2].y, // \n orderedTriangle.color);
					 **/
					// Graphics fill() implementation - slow down compared to wireframe
					// rendering but to be expected
					this.fillTriangle_GRAPHICS_IMPL(graphics, orderedTriangle);
					// Graphics draw() implements - fastest draw method
					// this.drawTriangle_GRAPHICS_IMPL(graphics, orderedTriangle);
				}
			}
		}
		graphics.dispose();
		bufferStrategy.show();
	}

	/**
	 * Draws triangle - helper function
	 */
	public void drawTriangle_GRAPHICS_IMPL(Graphics graphics, Triangle triangle) {
		this.drawTriangle_GRAPHICS_IMPL(graphics, triangle.vectors[0].x, triangle.vectors[0].y, triangle.vectors[1].x, triangle.vectors[1].y, triangle.vectors[2].x, triangle.vectors[2].y, triangle.color);
	}

	/**
	 * Draws triangle using the standard Java 2D graphics implementation - fastest
	 * available
	 */
	public void drawTriangle_GRAPHICS_IMPL(Graphics graphics, double px, double py, double sx, double sy, double tx, double ty, Color color) {
		graphics.setColor(color);
		graphics.drawPolygon(new int[] { (int) px, (int) sx, (int) tx }, new int[] { (int) py, (int) sy, (int) ty }, 3);
	}

	/**
	 * Fills triangle - helper function
	 */
	public void fillTriangle_GRAPHICS_IMPL(Graphics graphics, Triangle triangle) {
		this.fillTriangle_GRAPHICS_IMPL(graphics, triangle.vectors[0].x, triangle.vectors[0].y, triangle.vectors[1].x, triangle.vectors[1].y, triangle.vectors[2].x, triangle.vectors[2].y, triangle.color);
	}

	/**
	 * Fills triangle using the standard Java 2D graphics implementation - massive
	 * slow down, to optimize
	 */
	public void fillTriangle_GRAPHICS_IMPL(Graphics graphics, double px, double py, double sx, double sy, double tx, double ty, Color color) {
		graphics.setColor(color);
		graphics.fillPolygon(new int[] { (int) px, (int) sx, (int) tx }, new int[] { (int) py, (int) sy, (int) ty }, 3);
	}

	/**
	 * Own algorithm for drawing triangles - slightly slower than system
	 * implementation (why?) - No helper method because unused
	 */
	public void drawTriangle_LINE_BY_LINE(Graphics graphics, double px, double py, double sx, double sy, double tx, double ty, Color color) {
		graphics.setColor(color);
		graphics.drawLine((int) px, (int) py, (int) sx, (int) sy);
		graphics.drawLine((int) sx, (int) sy, (int) tx, (int) ty);
		graphics.drawLine((int) tx, (int) ty, (int) px, (int) py);
	}

	/**
	 * Returns the sum of the initial vertices as a new vertex
	 */
	public Vertex add(Vertex vertex_a, Vertex vertex_b) {
		return new Vertex(vertex_a.x + vertex_b.x, vertex_a.y + vertex_b.y, vertex_a.z + vertex_b.z);
	}

	/**
	 * Returns the difference of the initial vertices as a new vertex
	 */
	public Vertex subtract(Vertex vertex_a, Vertex vertex_b) {
		return new Vertex(vertex_a.x - vertex_b.x, vertex_a.y - vertex_b.y, vertex_a.z - vertex_b.z);
	}

	/**
	 * Returns initial vertex scaled up by k-factor as a new vertex
	 */
	public Vertex multiply(Vertex vertex, double k) {
		return new Vertex(vertex.x * k, vertex.y * k, vertex.z * k);
	}

	/**
	 * Returns initial vertex scaled down by k-factor as a new vertex
	 */
	public Vertex divide(Vertex vertex, double k) {
		return new Vertex(vertex.x / k, vertex.y / k, vertex.z / k);
	}

	/**
	 * Returns the dot product of the initial vertices - comparison tool (lighting)
	 */
	public double dotProduct(Vertex vertex_a, Vertex vertex_b) {
		return (vertex_a.x * vertex_b.x) + (vertex_a.y * vertex_b.y) + (vertex_a.z * vertex_b.z);
	}

	/**
	 * Returns the cross product of the initial vertices as a new vertex - useful
	 * for computing normals - result vertex will be perpendicular to plane created
	 * by the initial vertices
	 */
	public Vertex crossProduct(Vertex vertex_a, Vertex vertex_b) {
		double x = (vertex_a.y * vertex_b.z) - (vertex_a.z * vertex_b.y);
		double y = (vertex_a.z * vertex_b.x) - (vertex_a.x * vertex_b.z);
		double z = (vertex_a.x * vertex_b.y) - (vertex_a.y * vertex_b.x);
		return new Vertex(x, y, z);
	}

	/**
	 * Returns a normalized version of the initial vertex as a new vertex
	 */
	public Vertex normalize(Vertex vertex) {
		double length = this.length(vertex);
		return new Vertex(vertex.x / length, vertex.y / length, vertex.z / length);
	}

	/**
	 * Returns the length of the vertex
	 */
	public double length(Vertex vertex) {
		return Math.sqrt((vertex.x * vertex.x) + (vertex.y * vertex.y) + (vertex.z * vertex.z));
	}

	/**
	 * Converts a vertex to a 1-by-4 matrix
	 */
	public Matrix convertVertexToMatrix(Vertex vertex) {
		Matrix vectMat = new Matrix(1, 4);
		vectMat.data[0][0] = vertex.x;
		vectMat.data[0][1] = vertex.y;
		vectMat.data[0][2] = vertex.z;
		vectMat.data[0][3] = vertex.w;
		return vectMat;
	}

	/**
	 * Converts a 1-by-4 matrix to a vertex - need to check size
	 */
	public Vertex convertMatrixToVertex(Matrix matrix) {
		double x = matrix.data[0][0];
		double y = matrix.data[0][1];
		double z = matrix.data[0][2];
		double w = matrix.data[0][3];
		return new Vertex(x, y, z, w);
	}

	/**
	 * Converts a triangle to a 3-by-4 matrix
	 */
	public Matrix convertTriangleToMatrix(Triangle triangle) {
		Matrix triangleMat = new Matrix(3, 4);
		for (int index = 0; index < 3; index += 1) {
			triangleMat.data[index] = this.convertVertexToMatrix(triangle.vectors[index]).data[0];
		}
		return triangleMat;
	}

	/**
	 * Converts a 3-by-4 matrix to a triangle - need to check size
	 */
	public Triangle convertMatrixToTriangle(Matrix matrix, Color color) {
		Vertex firstVertex = new Vertex(matrix.data[0][0], matrix.data[0][1], matrix.data[0][2], matrix.data[0][3]);
		Vertex secondVertex = new Vertex(matrix.data[1][0], matrix.data[1][1], matrix.data[1][2], matrix.data[1][3]);
		Vertex thirdVertex = new Vertex(matrix.data[2][0], matrix.data[2][1], matrix.data[2][2], matrix.data[2][3]);
		return new Triangle(firstVertex, secondVertex, thirdVertex, color);
	}

	/**
	 * Returns the 1-by-4 * 4-by-4 matrices product as a new vertex - initial vertex
	 * is considered a 1-by-4 matrix - this implementation is messy and could be
	 * refactored to use matrix-matrix multiplication
	 */
	public Vertex applyMatrixToVector_PW(Vertex vertex, Matrix matrix) {
		double x = (vertex.x * matrix.data[0][0]) + (vertex.y * matrix.data[1][0]) + (vertex.z * matrix.data[2][0]) + (vertex.w * matrix.data[3][0]);
		double y = (vertex.x * matrix.data[0][1]) + (vertex.y * matrix.data[1][1]) + (vertex.z * matrix.data[2][1]) + (vertex.w * matrix.data[3][1]);
		double z = (vertex.x * matrix.data[0][2]) + (vertex.y * matrix.data[1][2]) + (vertex.z * matrix.data[2][2]) + (vertex.w * matrix.data[3][2]);
		double w = (vertex.x * matrix.data[0][3]) + (vertex.y * matrix.data[1][3]) + (vertex.z * matrix.data[2][3]) + (vertex.w * matrix.data[3][3]);
		return new Vertex(x, y, z, w);
	}

	/**
	 * See note above - refactoring of the function to use matrices product
	 */
	public Vertex applyMatrixToVector_MPW(Vertex vertex, Matrix matrix) {
		Matrix vectMat = this.convertVertexToMatrix(vertex);
		Matrix productMat = vectMat.multiply(matrix);
		return this.convertMatrixToVertex(productMat);
	}

	/**
	 * Returns the transformed-by-matrix triangle - this implementation is messy and
	 * could be refactored to use matrix-matrix multiplication
	 */
	public Triangle applyMatrixToTriangle_OW(Triangle triangle, Matrix matrix) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < 3; index += 1) {
			transformedTriangle.vectors[index] = this.applyMatrixToVector_MPW(transformedTriangle.vectors[index], matrix);
		}
		return transformedTriangle;
	}

	/**
	 * See note above - refactoring of the function to use matrices product
	 */
	public Triangle applyMatrixToTriangle_NW(Triangle triangle, Matrix matrix) {
		Matrix triangleMatrix = this.convertTriangleToMatrix(triangle);
		Matrix resultMatrix = triangleMatrix.multiply(matrix);
		return this.convertMatrixToTriangle(resultMatrix, triangle.color);
	}

	/**
	 * Returns a new triangle - sum of the initial triangle's vertices coordinates &
	 * the initial vertex - this implementation is messy and could be refactored
	 */
	public Triangle addVertexToTriangle(Triangle triangle, Vertex vertex) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < 3; index += 1) {
			transformedTriangle.vectors[index] = this.add(transformedTriangle.vectors[index], vertex);
		}
		return transformedTriangle;
	}

	/**
	 * Returns a new triangle - difference of the initial triangle's vertices
	 * coordinates & the initial vertex - this implementation is messy and could be
	 * refactored
	 */
	public Triangle subtractVertexFromTriangle(Triangle triangle, Vertex vertex) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < 3; index += 1) {
			transformedTriangle.vectors[index] = this.subtract(transformedTriangle.vectors[index], vertex);
		}
		return transformedTriangle;
	}

	/**
	 * Returns a new triangle - normalizes all the vectors of the initial triangle
	 */
	public Triangle normalizeTriangle(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < 3; index += 1) {
			transformedTriangle.vectors[index] = this.divide(transformedTriangle.vectors[index], transformedTriangle.vectors[index].w);
		}
		return transformedTriangle;
	}

	/**
	 * Returns the normal vertex to a triangle
	 */
	public Vertex normalToTriangle(Triangle triangle) {
		Vertex firstAxis = this.subtract(triangle.vectors[1], triangle.vectors[0]);
		Vertex secondAxis = this.subtract(triangle.vectors[2], triangle.vectors[0]);
		return this.crossProduct(firstAxis, secondAxis);
	}

	/**
	 * Scales a vertex to viewing distance
	 */
	public Vertex scaleVertexToView(Vertex vertex) {
		vertex.print();
		Vertex scaledVertex = new Vertex(vertex.x * Bresenham.WIDTH_SCALING_FACTOR, vertex.y * Bresenham.HEIGHT_SCALING_FACTOR, vertex.z);
		scaledVertex.print();
		return scaledVertex;
	}

	/**
	 * Scales a triangle to viewing distance
	 */
	public Triangle scaleTriangleToView(Triangle triangle) {
		Triangle transformedTriangle = new Triangle(triangle);
		for (int index = 0; index < 3; index += 1) {
			transformedTriangle.vectors[index].x *= Bresenham.WIDTH_SCALING_FACTOR;
			transformedTriangle.vectors[index].y *= Bresenham.HEIGHT_SCALING_FACTOR;
		}
		return transformedTriangle;
	}

	/**
	 * Returns the 4-by-4 projection (world->view) matrix - this is where the 3D to
	 * 2D magic takes place
	 */
	public Matrix createProjectionMatrix(double fieldOfViewInDegrees, double aspectRatio, double nearField, double farField) {
		Matrix projectionMatrix = new Matrix(4, 4);
		double fieldOfViewInRadians = Math.toRadians(fieldOfViewInDegrees);
		double tangentToFieldOfView = 1.0 / Math.tan(fieldOfViewInRadians * 0.5);
		projectionMatrix.data[0][0] = aspectRatio * tangentToFieldOfView;
		projectionMatrix.data[1][1] = tangentToFieldOfView;
		projectionMatrix.data[2][2] = farField / (farField - nearField);
		projectionMatrix.data[3][2] = (-farField * nearField) / (farField - nearField);
		projectionMatrix.data[2][3] = 1.0;
		projectionMatrix.data[3][3] = 0.0;
		return projectionMatrix;
	}

	/**
	 * Returns a 4-by-4 translation matrix - translation applied to every object in
	 * the world
	 */
	public Matrix createTranslationMatrix(double x, double y, double z) {
		Matrix translationMatrix = new Matrix(4, 4);
		translationMatrix.data[0][0] = 1.0;
		translationMatrix.data[1][1] = 1.0;
		translationMatrix.data[2][2] = 1.0;
		translationMatrix.data[3][3] = 1.0;
		translationMatrix.data[3][0] = x;
		translationMatrix.data[3][1] = y;
		translationMatrix.data[3][2] = z;
		return translationMatrix;
	}

	/**
	 * Returns a 4-by-4 rotation matrix around the x axis by the angle value -
	 * usually 0 when created
	 */
	public Matrix createXRotationMatrix(double angle) {
		Matrix xRotationMatrix = new Matrix(4, 4);
		xRotationMatrix.data[0][0] = 1.0;
		xRotationMatrix.data[1][1] = Math.cos(angle);
		xRotationMatrix.data[1][2] = Math.sin(angle);
		xRotationMatrix.data[2][1] = -Math.sin(angle);
		xRotationMatrix.data[2][2] = Math.cos(angle);
		xRotationMatrix.data[3][3] = 1.0;
		return xRotationMatrix;
	}

	/**
	 * Returns a 4-by-4 rotation matrix around the y axis by the angle value -
	 * usually 0 when created - should stay ineffective
	 */
	public Matrix createYRotationMatrix(double angle) {
		Matrix yRotationMatrix = new Matrix(4, 4);
		yRotationMatrix.data[0][0] = Math.cos(angle);
		yRotationMatrix.data[0][2] = Math.sin(angle);
		yRotationMatrix.data[2][0] = -Math.sin(angle);
		yRotationMatrix.data[1][1] = 1.0;
		yRotationMatrix.data[2][2] = Math.cos(angle);
		yRotationMatrix.data[3][3] = 1.0;
		return yRotationMatrix;
	}

	/**
	 * Returns a 4-by-4 rotation matrix around the z axis by the angle value -
	 * usually 0 when created
	 */
	public Matrix createZRotationMatrix(double angle) {
		Matrix zRotationMatrix = new Matrix(4, 4);
		zRotationMatrix.data[0][0] = Math.cos(angle);
		zRotationMatrix.data[0][1] = Math.sin(angle);
		zRotationMatrix.data[1][0] = -Math.sin(angle);
		zRotationMatrix.data[1][1] = Math.cos(angle);
		zRotationMatrix.data[2][2] = 1.0;
		zRotationMatrix.data[3][3] = 1.0;
		return zRotationMatrix;
	}

	/**
	 * Updates the x-rotation matrix to match the value of the new angle - must be
	 * called every cycle
	 */
	public void updateXRotationMatrix(Matrix xMatrix, double angle) {
		xMatrix.data[0][0] = 1.0;
		xMatrix.data[1][1] = Math.cos(angle);
		xMatrix.data[1][2] = Math.sin(angle);
		xMatrix.data[2][1] = -Math.sin(angle);
		xMatrix.data[2][2] = Math.cos(angle);
		xMatrix.data[3][3] = 1.0;
	}

	/**
	 * Updates the y-rotation matrix to match the value of the new angle - must be
	 * called every cycle but is never currently used
	 */
	public void updateYRotationMatrix(Matrix yMatrix, double angle) {
		yMatrix.data[0][0] = Math.cos(angle);
		yMatrix.data[0][2] = Math.sin(angle);
		yMatrix.data[2][0] = -Math.sin(angle);
		yMatrix.data[1][1] = 1.0;
		yMatrix.data[2][2] = Math.cos(angle);
		yMatrix.data[3][3] = 1.0;
	}

	/**
	 * Updates the z-rotation matrix to match the value of the new angle - must be
	 * called every cycle
	 */
	public void updateZRotationMatrix(Matrix zMatrix, double angle) {
		zMatrix.data[0][0] = Math.cos(angle);
		zMatrix.data[0][1] = Math.sin(angle);
		zMatrix.data[1][0] = -Math.sin(angle);
		zMatrix.data[1][1] = Math.cos(angle);
		zMatrix.data[2][2] = 1.0;
		zMatrix.data[3][3] = 1.0;
	}

	/**
	 * Returns the "eye" matrix relative to the object position and an "up" vertex
	 */
	public Matrix pointAt(Vertex position, Vertex target, Vertex reference) {
		Matrix resultMatrix = new Matrix(4, 4);
		// Calculate new forward direction relative to position
		Vertex forwardP = this.normalize(this.subtract(target, position));
		// Calculate new up direction relative to new forward
		Vertex scaleF = this.multiply(forwardP, this.dotProduct(reference, forwardP));
		Vertex upP = this.normalize(this.subtract(reference, scaleF));
		// Calculate new right as cross product of forward & up
		Vertex rightP = this.crossProduct(upP, forwardP);
		// Construct "point-at" matrix
		{
			// First row
			resultMatrix.data[0][0] = rightP.x;
			resultMatrix.data[0][1] = rightP.y;
			resultMatrix.data[0][2] = rightP.z;
			resultMatrix.data[0][3] = 0.0;
			// Second row
			resultMatrix.data[1][0] = upP.x;
			resultMatrix.data[1][1] = upP.y;
			resultMatrix.data[1][2] = upP.z;
			resultMatrix.data[1][3] = 0.0;
			// Third row
			resultMatrix.data[2][0] = forwardP.x;
			resultMatrix.data[2][1] = forwardP.y;
			resultMatrix.data[2][2] = forwardP.z;
			resultMatrix.data[2][3] = 0.0;
			// Fourth row
			resultMatrix.data[3][0] = position.x;
			resultMatrix.data[3][1] = position.y;
			resultMatrix.data[3][2] = position.z;
			resultMatrix.data[3][3] = 1.0;
		}
		return resultMatrix;
	}

	/**
	 * Quick inverse for 4-by-4 matrices - messy - refactor - only works for
	 * rotation/translation matrices
	 */
	public Matrix quickInverse(Matrix matrix) {
		Matrix resultMatrix = new Matrix(4, 4);
		// Fill inverse matrix
		{
			// First row
			resultMatrix.data[0][0] = matrix.data[0][0];
			resultMatrix.data[0][1] = matrix.data[1][0];
			resultMatrix.data[0][2] = matrix.data[2][0];
			resultMatrix.data[0][3] = 0.0;
			// Second row
			resultMatrix.data[1][0] = matrix.data[0][1];
			resultMatrix.data[1][1] = matrix.data[1][1];
			resultMatrix.data[1][2] = matrix.data[2][1];
			resultMatrix.data[1][3] = 0.0;
			// Third row
			resultMatrix.data[2][0] = matrix.data[0][2];
			resultMatrix.data[2][1] = matrix.data[1][2];
			resultMatrix.data[2][2] = matrix.data[2][2];
			resultMatrix.data[2][3] = 0.0;
			// Fourth row
			resultMatrix.data[3][0] = -((matrix.data[3][0] * resultMatrix.data[0][0]) + (matrix.data[3][1] * resultMatrix.data[1][0]) + (matrix.data[3][2] * resultMatrix.data[2][0]));
			resultMatrix.data[3][1] = -((matrix.data[3][0] * resultMatrix.data[0][1]) + (matrix.data[3][1] * resultMatrix.data[1][1]) + (matrix.data[3][2] * resultMatrix.data[2][1]));
			resultMatrix.data[3][2] = -((matrix.data[3][0] * resultMatrix.data[0][2]) + (matrix.data[3][1] * resultMatrix.data[1][2]) + (matrix.data[3][2] * resultMatrix.data[2][2]));
			resultMatrix.data[3][3] = 1.0;
		}
		return matrix;
	}

	/**
	 * Lowers the rendering quality of the graphics object as much as possible - no
	 * noticeable performance gain
	 */
	public void decreaseRenderQuality(Graphics graphics) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	}

	/**
	 * Highers the rendering quality of the graphics object as much as possible - no
	 * noticeable performance loss
	 */
	public void increaseRenderQuality(Graphics graphics) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	public static void main(String[] args) {
		new Bresenham().start();
	}
}
