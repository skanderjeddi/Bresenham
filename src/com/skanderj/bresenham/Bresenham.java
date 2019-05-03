package com.skanderj.bresenham;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import com.skanderj.bresenham.math.Matrix;
import com.skanderj.bresenham.math.Vertex;
import com.skanderj.gingerbread.Process;
import com.skanderj.gingerbread.core.Game;

public final class Bresenham extends Game {
	// Singleton model
	private static Bresenham instance;

	public static final Bresenham getInstance() {
		return Bresenham.instance == null ? Bresenham.instance = new Bresenham() : Bresenham.instance;
	}

	// General properties
	public static final String PROCESS_IDENTIFIER = "bresenham";
	public static final double UPDATES_PER_SECOND = 60.0;
	public static final double WIDTH = 250, HEIGHT = 250;

	// Window properties
	public static final String WINDOW_TITLE = "Bresenham";
	public static final int ORIGIN_COORD_X = 0, ORIGIN_COORD_Y = 0;
	public static final int SCALE = 3, BUFFER_MODE = 2;
	public static final int WINDOW_WIDTH = (int) (Bresenham.WIDTH * Bresenham.SCALE), WINDOW_HEIGHT = (int) (Bresenham.HEIGHT * Bresenham.SCALE);

	// 3D properties
	public static final double FIELD_OF_VIEW_IN_DEGREES = 90, ASPECT_RATIO = Bresenham.HEIGHT / Bresenham.WIDTH, NEAR_FIELD = 0.1, FAR_FIELD = 1000.0;
	public static final double HORIZONTAL_SCALING_FACTOR = 0.5 * Bresenham.WINDOW_WIDTH, VERTICAL_SCALING_FACTOR = 0.5 * Bresenham.WINDOW_HEIGHT;

	// Matrices
	private Matrix projectionMatrix, translationMatrix, zRotationMatrix, xRotationMatrix, worldMatrix, cameraMatrix, viewMatrix;
	private double rotationAngle;

	// Individual vertices
	private Vertex cameraLocation, lightDirection, upAxis, gaze, target;
	private double yaw;

	private Mesh mainMesh;
	private String meshFileName;

	private Bresenham() {
		super(Bresenham.PROCESS_IDENTIFIER, Bresenham.UPDATES_PER_SECOND, Bresenham.WINDOW_TITLE, Bresenham.WINDOW_WIDTH, Bresenham.WINDOW_HEIGHT, Bresenham.BUFFER_MODE);
		this.meshFileName = "teapot.obj";
	}

	@Override
	protected void create() {
		// Initialise camera location & lighting
		{
			// Camera location as vertex
			this.cameraLocation = new Vertex(0.0, 0.0, 0.0);
			// Light direction as negative z axis - "coming towards the player" to allow
			// lighting
			this.lightDirection = new Vertex(0.0, 1.0, -1.0);
			// Normalise light vertex
			this.lightDirection = Vertex.normalize(this.lightDirection);
			// Up vertex
			this.upAxis = new Vertex(0.0, 1.0, 0.0, 1.0);
			// Gaze
			this.gaze = new Vertex(0.0, 0.0, 1.0);
			// Target
			this.target = new Vertex(0.0, 0.0, 1.0);
			// Yaw
			this.yaw = 0.0;
		}
		// Initialise program matrices
		{
			// Projection matrix
			this.projectionMatrix = this.createProjectionMatrix(Bresenham.FIELD_OF_VIEW_IN_DEGREES, Bresenham.ASPECT_RATIO, Bresenham.NEAR_FIELD, Bresenham.FAR_FIELD);
			// Translation matrix
			this.translationMatrix = this.createTranslationMatrix(0.0, 0.0, 8.0);
			// Initialise rotation angle
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
			this.mainMesh = Mesh.loadFromFile(this.meshFileName);
		} catch (NumberFormatException | IOException exception) {
			exception.printStackTrace();
			// Can't load mesh so exit
			System.exit(Process.EXIT_FAILURE);
		}
		super.create();
	}

	@Override
	protected void destroy() {
		super.destroy();
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
					this.cameraLocation.y -= 0.5 * delta;
				}
				if (downKeyHeld) {
					this.cameraLocation.y += 0.5 * delta;
				}
				if (rightKeyHeld) {
					this.cameraLocation.x += 0.5 * delta;
				}
				if (leftKeyHeld) {
					this.cameraLocation.x -= 0.5 * delta;
				}
			}
			Vertex scaledDirection = Vertex.multiply(this.gaze, 0.5 * delta);
			// Handle camera rotation
			{
				// Turn left
				if (qKeyHeld) {
					this.yaw += 0.05 * delta;
				}
				// Turn right
				if (dKeyHeld) {
					this.yaw -= 0.05 * delta;
				}
				// Go forward
				if (zKeyHeld) {
					this.cameraLocation = Vertex.subtract(this.cameraLocation, scaledDirection);
				}
				// Go backwards
				if (sKeyHeld) {
					this.cameraLocation = Vertex.add(this.cameraLocation, scaledDirection);
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
		}
		// Create transformation matrix
		{
			// Z * X rotation product
			this.worldMatrix = Matrix.multiply(this.zRotationMatrix, this.xRotationMatrix);
			// Translation
			this.worldMatrix = Matrix.multiply(this.worldMatrix, this.translationMatrix);
		}
		// Camera handling
		{
			this.target = new Vertex(0.0, 0.0, 1.0);
			Matrix cameraRotationMatrix = this.createYRotationMatrix(this.yaw);
			this.gaze = Vertex.applyMatrixToVector_MPW(this.target, cameraRotationMatrix);
			this.target = Vertex.add(this.cameraLocation, this.gaze);
			this.cameraMatrix = this.pointAt(this.cameraLocation, this.target, this.upAxis);
			// Uses quick inverse - does real inverse work? Is it slower?
			// this.viewMatrix = this.quickInverse(this.cameraMatrix);
			this.viewMatrix = this.realInverse(this.cameraMatrix);
		}
		// Update keyboard & mouse
		{
			this.keyboard.update();
			this.mouse.update();
		}
	}

	@Override
	public void render(Graphics graphics) {
		// Clear the screen
		graphics.setColor(Color.BLACK);
		graphics.fillRect(Bresenham.ORIGIN_COORD_X, Bresenham.ORIGIN_COORD_X, Bresenham.WINDOW_WIDTH, Bresenham.WINDOW_HEIGHT);
		// Set worse rendering hints (possibly optional?)
		this.increaseRenderQuality(graphics);
		// Triangles transform
		Vector<Triangle> queueVector = new Vector<Triangle>();
		{
			// Parse triangles in cube mesh
			for (Triangle triangle : this.mainMesh.triangles) {
				// Copy data from current triangle - we don't want to change the original data!
				Triangle localTriangle = new Triangle(triangle);
				// Transform localTriangle
				localTriangle = Triangle.applyMatrixToTriangle_NW(localTriangle, this.worldMatrix);
				// Calculate normal data
				Vertex normalVertex = Vertex.normalToTriangle(localTriangle);
				normalVertex = Vertex.normalize(normalVertex);
				// Calculate camera ray
				Vertex cameraRay = Vertex.subtract(localTriangle.vertices[0], this.cameraLocation);
				// Calculate dot product to evaluate if triangle is in view
				double normalCameraDotProduct = Vertex.dotProduct(normalVertex, cameraRay);
				if (normalCameraDotProduct < 0.0) {
					// Set colour
					float dotProduct = (float) Math.max(0.1f, Vertex.dotProduct(this.lightDirection, normalVertex));
					localTriangle.color = new Color(dotProduct, dotProduct, dotProduct);
					// Transform world space to view space
					localTriangle = Triangle.applyMatrixToTriangle_NW(localTriangle, this.viewMatrix);
					// Multiply by protection matrix 3D -> 2D
					localTriangle = Triangle.applyMatrixToTriangle_NW(localTriangle, this.projectionMatrix);
					// Normalise
					localTriangle = Triangle.normalizeTriangle(localTriangle);
					// Scale into view
					Vertex viewOffset = new Vertex(1.0, 1.0, 0.0);
					localTriangle = Triangle.addVertexToTriangle(localTriangle, viewOffset);
					localTriangle = Triangle.scaleTriangleToView(localTriangle);
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

	/**
	 * Draws triangle - helper function
	 */
	public void drawTriangle_GRAPHICS_IMPL(Graphics graphics, Triangle triangle) {
		this.drawTriangle_GRAPHICS_IMPL(graphics, triangle.vertices[0].x, triangle.vertices[0].y, triangle.vertices[1].x, triangle.vertices[1].y, triangle.vertices[2].x, triangle.vertices[2].y, triangle.color);
	}

	/**
	 * Draws triangle using the standard Java 2D graphics implementation - fastest
	 * available
	 */
	public void drawTriangle_GRAPHICS_IMPL(Graphics graphics, double firstX, double firstY, double secondX, double secondY, double thirdX, double thirdY, Color color) {
		graphics.setColor(color);
		graphics.drawPolygon(new int[] { (int) firstX, (int) secondX, (int) thirdX }, new int[] { (int) firstY, (int) secondY, (int) thirdY }, 3);
	}

	/**
	 * Fills triangle - helper function
	 */
	public void fillTriangle_GRAPHICS_IMPL(Graphics graphics, Triangle triangle) {
		this.fillTriangle_GRAPHICS_IMPL(graphics, triangle.vertices[0].x, triangle.vertices[0].y, triangle.vertices[1].x, triangle.vertices[1].y, triangle.vertices[2].x, triangle.vertices[2].y, triangle.color);
	}

	/**
	 * Fills triangle using the standard Java 2D graphics implementation - massive
	 * slow down, to optimise
	 */
	public void fillTriangle_GRAPHICS_IMPL(Graphics graphics, double firstX, double firstY, double secondX, double secondY, double thirdX, double thirdY, Color color) {
		graphics.setColor(color);
		graphics.fillPolygon(new int[] { (int) firstX, (int) secondX, (int) thirdX }, new int[] { (int) firstY, (int) secondY, (int) thirdY }, 3);
	}

	/**
	 * Own algorithm for drawing triangles - slightly slower than system
	 * implementation (why?) - No helper method because unused
	 */
	public void drawTriangle_LINE_BY_LINE(Graphics graphics, double firstX, double firstY, double secondX, double secondY, double thirdX, double thirdY, Color color) {
		graphics.setColor(color);
		graphics.drawLine((int) firstX, (int) firstY, (int) secondX, (int) secondY);
		graphics.drawLine((int) secondX, (int) secondY, (int) thirdX, (int) thirdY);
		graphics.drawLine((int) thirdY, (int) thirdX, (int) firstX, (int) firstY);
	}

	/**
	 * Returns shortest distance from point to plane, plane normal must be
	 * normalised
	 */
	public double distancePointToPlane(Vertex planePoint, Vertex planeNormal, Vertex target) {
		target = Vertex.normalize(target);
		return (((planeNormal.x * target.x) + (planeNormal.y * target.y) + (planeNormal.z * target.z)) - Vertex.dotProduct(planeNormal, planePoint));
	}

	/**
	 *
	 */
	public int clipAgainstPlane(Vertex planePoint, Vertex planeNormal, Triangle input, Triangle firstOutput, Triangle secondOutput) {
		planeNormal = Vertex.normalize(planeNormal);
		return -1;
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
		Vertex forwardP = Vertex.normalize(Vertex.subtract(target, position));
		// Calculate new up direction relative to new forward
		Vertex scaleF = Vertex.multiply(forwardP, Vertex.dotProduct(reference, forwardP));
		Vertex upP = Vertex.normalize(Vertex.subtract(reference, scaleF));
		// Calculate new right as cross product of forward & up
		Vertex rightP = Vertex.crossProduct(upP, forwardP);
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
		return resultMatrix;
	}

	/**
	 * Real inverse of any n-by-n matrix
	 */
	public Matrix realInverse(Matrix matrix) {
		return Matrix.inverse(matrix);
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
	 * Increases the rendering quality of the graphics object as much as possible -
	 * no noticeable performance loss
	 */
	public void increaseRenderQuality(Graphics graphics) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		// graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}
}
