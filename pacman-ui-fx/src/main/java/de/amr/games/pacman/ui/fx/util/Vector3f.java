/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.math.Vector2f;

/**
 * Immutable 3D vector with float precision. No full-fledged implementation, just the needed methods.
 * 
 * @author Armin Reichert
 */
public class Vector3f {

	float x; float y; float z;

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float z() {
		return z;
	}

	public Vector3f(Vector3f v) {
		this(v.x, v.y, v.z);
	}

	public Vector3f(Vector2f v, float z) {
		this(v.x(), v.y(), z);
	}

	public Vector2f toVector2f() {
		return new Vector2f(x, y);
	}

	/**
	 * Computes the dot product of this vector and the given vector.
	 *
	 * @param v other vector
	 * @return the dot product
	 */
	public float dot(Vector3f v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * @return the length of this vector
	 */
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * @return normalized (length 1) vector
	 */
	public Vector3f normalized() {
		float norm = 1.0f / length();
		return new Vector3f(x * norm, y * norm, z * norm);
	}
}