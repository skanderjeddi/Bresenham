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
import com.skanderj.bresenham.math.Vector4D;
import com.skanderj.gingerbread.SimpleThread;
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

	// Individual vectors
	private Vector4D cameraLocation, lightDirection, upAxis, gaze, target;
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
			// Camera location as vector
			this.cameraLocation = new Vector4D(0.0, 0.0, 0.0);
			// Light direction as negative z axis - "coming towards the player" to allow
			// lighting
			this.lightDirection = new Vector4D(0.0, 1.0, -1.0);
			// Normalise light vector
			this.lightDirection = Vector4D.normalize(this.lightDirection);
			// Up vector
			this.upAxis = new Vector4D(0.0, 1.0, 0.0, 1.0);
			// Gaze
			this.gaze = new Vector4D(0.0, 0.0, 1.0);
			// Target
			this.target = new Vector4D(0.0, 0.0, 1.0);
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
			System.exit(SimpleThread.EXIT_FAILURE);
		}
		super.create();
	}

	@Override
	protected void destroy() {
		super.destroy();
		System.exit(SimpleThread.EXIT_SUCCESS);
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
			Vector4D scaledDirection = Vector4D.multiply(this.gaze, 0.5 * delta);
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
					this.cameraLocation = Vector4D.add(this.cameraLocation, scaledDirection);
				}
				// Go backwards
				if (sKeyHeld) {
					this.cameraLocation = Vector4D.subtract(this.cameraLocation, scaledDirection);
				}
			}
		}
		// Update rotation angle (optional)
		{
			// this.rotationAngle += 0.05 * delta;
		}
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
			this.worldMatrix = Matrix.product(this.zRotationMatrix, this.xRotationMatrix);
			// Translation
			this.worldMatrix = Matrix.product(this.worldMatrix, this.translationMatrix);
		}
		// Camera handling
		{
			this.target = new Vector4D(0.0, 0.0, 1.0);
			Matrix cameraRotationMatrix = this.createYRotationMatrix(this.yaw);
			this.gaze = Vector4D.applyMatrixToVector_MPW(this.target, cameraRotationMatrix);
			this.target = Vector4D.add(this.cameraLocation, this.gaze);
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
		graphics.fillRect(Bresenham.ORIGIN_COORD_X, Bresenham.ORIGIN_COORD_Y, Bresenham.WINDOW_WIDTH, Bresenham.WINDOW_HEIGHT);
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
				Vector4D normalVector = Vector4D.normalToTriangle(localTriangle);
				normalVector = Vector4D.normalize(normalVector);
				// Calculate camera ray
				Vector4D cameraRay = Vector4D.subtract(localTriangle.vectors[0], this.cameraLocation);
				// Calculate dot product to evaluate if triangle is in view
				double normalCameraDotProduct = Vector4D.dotProduct(normalVector, cameraRay);
				if (normalCameraDotProduct < 0.0) {
					// Set colour
					float dotProduct = (float) Math.max(0.1f, Vector4D.dotProduct(this.lightDirection, normalVector));
					localTriangle.color = new Color(dotProduct, dotProduct, dotProduct);
					// Transform world space to view space
					localTriangle = Triangle.applyMatrixToTriangle_NW(localTriangle, this.viewMatrix);
					// Clipping
					Vector<Triangle> newTriangles = this.clipAgainstPlane(new Vector4D(0.0, 0.0, 0.001), new Vector4D(0.0, 0.0, 1.0), localTriangle);
					for (Triangle clippedTriangle : newTriangles) {
						// Multiply by protection matrix 3D -> 2D
						localTriangle = Triangle.applyMatrixToTriangle_NW(clippedTriangle, this.projectionMatrix);
						// Normalise
						localTriangle = Triangle.normalizeTriangle(localTriangle);
						// Flip XY
						localTriangle = Triangle.flipXYCoordinates(localTriangle);
						// Scale into view
						Vector4D viewOffset = new Vector4D(1.0, 1.0, 0.0);
						localTriangle = Triangle.addVectorToTriangle(localTriangle, viewOffset);
						localTriangle = Triangle.scaleTriangleToView(localTriangle);
						// Add to vector
						queueVector.add(localTriangle);
					}
				}
			}
		}
		// Sort all the vectors
		Collections.sort(queueVector);
		// Draw sorted triangles
		{
			for (Triangle orderedTriangle : queueVector) {
				Vector<Triangle> newTriangles = new Vector<Triangle>();
				// Add initial triangle
				newTriangles.add(orderedTriangle);
				int newTrianglesCount = 1;
				for (int plane = 0; plane < 4; plane += 1) {
					while (newTrianglesCount > 0) {
						// Take triangle from front of queue
						Triangle currentElement = newTriangles.firstElement();
						newTriangles.remove(0);
						newTrianglesCount -= 1;
						// Clip it against a plane. We only need to test each
						// subsequent plane, against subsequent new triangles
						// as all triangles after a plane clip are guaranteed
						// to lie on the inside of the plane. I like how this
						// comment is almost completely and utterly justified
						switch (plane) {
						case 0:
							newTriangles.addAll(this.clipAgainstPlane(new Vector4D(0.0, 0.0, 0.0), new Vector4D(0.0, 1.0, 0.0), currentElement));
							break;
						case 1:
							newTriangles.addAll(this.clipAgainstPlane(new Vector4D(0.0, Bresenham.WINDOW_HEIGHT - 1, 0.0), new Vector4D(0.0, -1.0, 0.0), currentElement));
							break;
						case 2:
							newTriangles.addAll(this.clipAgainstPlane(new Vector4D(0.0, 0.0, 0.0), new Vector4D(1.0, 0.0, 0.0), currentElement));
							break;
						case 3:
							newTriangles.addAll(this.clipAgainstPlane(new Vector4D(Bresenham.WINDOW_WIDTH - 1, 0.0, 0.0), new Vector4D(-1.0, 0.0, 0.0), currentElement));
							break;
						}
						// Clipping may yield a variable number of triangles, so
						// add these new ones to the back of the queue for subsequent
						// clipping against next planes
					}
					newTrianglesCount = newTriangles.size();
				}
				// Draw the transformed, viewed, clipped, projected, sorted, clipped triangles
				for (Triangle currentTriangle : newTriangles) {
					graphics.setColor(currentTriangle.color);
					this.fillTriangle(graphics, currentTriangle);
				}
			}
		}
	}

	/**
	 * Draws triangle - helper function
	 */
	public void drawTriangle(Graphics graphics, Triangle triangle) {
		this.drawTriangle(graphics, triangle.vectors[0].x, triangle.vectors[0].y, triangle.vectors[1].x, triangle.vectors[1].y, triangle.vectors[2].x, triangle.vectors[2].y, triangle.color);
	}

	/**
	 * Draws triangle using the standard Java 2D graphics implementation - fastest
	 * available
	 */
	public void drawTriangle(Graphics graphics, double firstX, double firstY, double secondX, double secondY, double thirdX, double thirdY, Color color) {
		graphics.setColor(color);
		graphics.drawPolygon(new int[] { (int) firstX, (int) secondX, (int) thirdX }, new int[] { (int) firstY, (int) secondY, (int) thirdY }, 3);
	}

	/**
	 * Fills triangle - helper function
	 */
	public void fillTriangle(Graphics graphics, Triangle triangle) {
		this.fillTriangle(graphics, triangle.vectors[0].x, triangle.vectors[0].y, triangle.vectors[1].x, triangle.vectors[1].y, triangle.vectors[2].x, triangle.vectors[2].y, triangle.color);
	}

	/**
	 * Fills triangle using the standard Java 2D graphics implementation - massive
	 * slow down, to optimise
	 */
	public void fillTriangle(Graphics graphics, double firstX, double firstY, double secondX, double secondY, double thirdX, double thirdY, Color color) {
		graphics.setColor(color);
		graphics.fillPolygon(new int[] { (int) firstX, (int) secondX, (int) thirdX }, new int[] { (int) firstY, (int) secondY, (int) thirdY }, 3);
	}

	/**
	 * Returns shortest distance from point to plane, plane normal must be
	 * normalised
	 */
	public double distancePointToPlane(Vector4D planePoint, Vector4D planeNormal, Vector4D target) {
		target = Vector4D.normalize(target);
		return (((planeNormal.x * target.x) + (planeNormal.y * target.y) + (planeNormal.z * target.z)) - Vector4D.dotProduct(planeNormal, planePoint));
	}

	/**
	 * \// TODO
	 */
	public Vector<Triangle> clipAgainstPlane(Vector4D planePoint, Vector4D planeNormal, Triangle input) {
		Vector<Triangle> newTriangles = new Vector<Triangle>();
		planeNormal = Vector4D.normalize(planeNormal);
		Vector4D[] pointsInside = new Vector4D[3], pointsOutside = new Vector4D[3];
		int pointsInsideCount = 0, pointsOutsideCount = 0;
		double firstDistance = this.distancePointToPlane(planePoint, planeNormal, input.vectors[0]);
		double secondDistance = this.distancePointToPlane(planePoint, planeNormal, input.vectors[1]);
		double thirdDistance = this.distancePointToPlane(planePoint, planeNormal, input.vectors[2]);
		if (firstDistance >= 0) {
			pointsInside[pointsInsideCount] = input.vectors[0];
			pointsInsideCount += 1;
		} else {
			pointsOutside[pointsOutsideCount] = input.vectors[0];
			pointsOutsideCount += 1;
		}
		if (secondDistance >= 0) {
			pointsInside[pointsInsideCount] = input.vectors[1];
			pointsInsideCount += 1;
		} else {
			pointsOutside[pointsOutsideCount] = input.vectors[1];
			pointsOutsideCount += 1;
		}
		if (thirdDistance >= 0) {
			pointsInside[pointsInsideCount] = input.vectors[2];
			pointsInsideCount += 1;
		} else {
			pointsOutside[pointsOutsideCount] = input.vectors[2];
			pointsOutsideCount += 1;
		}
		if (pointsInsideCount == 0) {
			return newTriangles;
		} else if (pointsInsideCount == 3) {
			newTriangles.add(input);
		}
		if ((pointsInsideCount == 1) && (pointsOutsideCount == 2)) {
			Vector4D firstPoint = pointsInside[0];
			Vector4D secondPoint = Vector4D.vectorPlaneIntersection(planePoint, planeNormal, pointsInside[0], pointsOutside[0]);
			Vector4D thirdPoint = Vector4D.vectorPlaneIntersection(planePoint, planeNormal, pointsInside[0], pointsOutside[1]);
			newTriangles.add(new Triangle(firstPoint, secondPoint, thirdPoint, input.color));
		}
		if ((pointsInsideCount == 2) && (pointsOutsideCount == 1)) {
			Vector4D firstTriangleFirstPoint = pointsInside[0];
			Vector4D firstTriangleSecondPoint = pointsInside[1];
			Vector4D firstTriangleThirdPoint = Vector4D.vectorPlaneIntersection(planePoint, planeNormal, pointsInside[0], pointsOutside[0]);
			Vector4D secondTriangleFirstPoint = pointsInside[1];
			Vector4D secondTriangleSecondPoint = firstTriangleThirdPoint;
			Vector4D secondTriangleThirdPoint = Vector4D.vectorPlaneIntersection(planePoint, planeNormal, pointsInside[1], pointsOutside[0]);
			newTriangles.add(new Triangle(firstTriangleFirstPoint, firstTriangleSecondPoint, firstTriangleThirdPoint, input.color));
			newTriangles.add(new Triangle(secondTriangleFirstPoint, secondTriangleSecondPoint, secondTriangleThirdPoint, input.color));
		}
		return newTriangles;
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
	 * Returns the "eye" matrix relative to the object position and an "up" vector
	 */
	public Matrix pointAt(Vector4D position, Vector4D target, Vector4D reference) {
		Matrix resultMatrix = new Matrix(4, 4);
		// Calculate new forward direction relative to position
		Vector4D forwardP = Vector4D.normalize(Vector4D.subtract(target, position));
		// Calculate new up direction relative to new forward
		Vector4D scaleF = Vector4D.multiply(forwardP, Vector4D.dotProduct(reference, forwardP));
		Vector4D upP = Vector4D.normalize(Vector4D.subtract(reference, scaleF));
		// Calculate new right as cross product of forward & up
		Vector4D rightP = Vector4D.crossProduct(upP, forwardP);
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
