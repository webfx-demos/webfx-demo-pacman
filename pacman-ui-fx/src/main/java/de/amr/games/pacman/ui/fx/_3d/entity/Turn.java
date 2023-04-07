/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.lib.steering.Direction;

/**
 * @author Armin Reichert
 */
public class Turn {

	private static final byte L = 0;
	private static final byte U = 1;
	private static final byte R = 2;
	private static final byte D = 3;

	//@formatter:off
	public static final byte[][][] TURNS = {
		{ null,    {L, R}, {L, U},  {L, -U} }, // LEFT  -> *
		{ {R, L},  null,   {R, U},  {R, D}  }, // RIGHT -> *
		{ {U, L},  {U, R}, null,    {U, D}  }, // UP    -> *
		{ {-U, L}, {D, R}, {-U, U}, null    }, // DOWN  -> *
	};

	public static byte dirIndex(Direction dir) {
		return switch (dir) {	case LEFT -> L;	case RIGHT -> R; case UP -> U; case DOWN -> D; default -> L; };
	}
	//@formatter:on

	public static double angle(byte dirIndex) {
		return dirIndex * 90.0;
	}

	public static double angle(Direction dir) {
		return angle(dirIndex(dir));
	}
}