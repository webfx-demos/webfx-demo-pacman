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
package de.amr.games.pacman.ui.fx.scene;

import java.util.Objects;

/**
 * @author Armin Reichert
 *
 */
public class GameSceneChoice {

	GameScene scene2D; GameScene scene3D;

	public GameSceneChoice(GameScene scene2D, GameScene scene3D) {
		this.scene2D = scene2D;
		this.scene3D = scene3D;
	}

	public GameSceneChoice(GameScene scene2D) {
		this(scene2D, null);
	}

	public GameScene scene2D() {
		return scene2D;
	}

	public GameScene scene3D() {
		return scene3D;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GameSceneChoice that = (GameSceneChoice) o;

		if (!Objects.equals(scene2D, that.scene2D)) return false;
		return Objects.equals(scene3D, that.scene3D);
	}

	@Override
	public int hashCode() {
		int result = scene2D != null ? scene2D.hashCode() : 0;
		result = 31 * result + (scene3D != null ? scene3D.hashCode() : 0);
		return result;
	}
}