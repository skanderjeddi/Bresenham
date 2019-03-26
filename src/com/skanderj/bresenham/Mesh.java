package com.skanderj.bresenham;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import com.skanderj.bresenham.math.Triangle;
import com.skanderj.bresenham.math.Vertex;

public class Mesh {
	/**
	 * Load a mesh from a waveform .obj file
	 *
	 * @param objName
	 * @return a constructed mesh from the data
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static final Mesh loadFromFile(String objName) throws NumberFormatException, IOException {
		Vector<Vertex> vectors = new Vector<Vertex>();
		Vector<Triangle> triangles = new Vector<Triangle>();
		File objFile = new File(objName);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(objFile));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			} else if (line.startsWith("v")) {
				String[] parts = line.split("\\s+");
				double x = Double.valueOf(parts[1]);
				double y = Double.valueOf(parts[2]);
				double z = Double.valueOf(parts[3]);
				Vertex vector = new Vertex(x, y, z);
				vectors.add(vector);
			} else if (line.startsWith("f")) {
				int[] indices = new int[3];
				String[] parts = line.split("\\s+");
				indices[0] = Integer.valueOf(parts[1]);
				indices[1] = Integer.valueOf(parts[2]);
				indices[2] = Integer.valueOf(parts[3]);
				triangles.add(new Triangle(vectors.get(indices[0] - 1), vectors.get(indices[1] - 1), vectors.get(indices[2] - 1), Color.WHITE));
			}
		}
		bufferedReader.close();
		return new Mesh(triangles.toArray(new Triangle[triangles.size()]));
	}

	protected final Triangle[] triangles;

	public Mesh(Triangle[] array) {
		this.triangles = new Triangle[array.length];
		for (int index = 0; index < array.length; index += 1) {
			this.triangles[index] = array[index];
		}
	}

	/**
	 * shoutout nathan maniquaire
	 */
}
