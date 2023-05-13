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

package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GameSceneConfiguration {

	private static final byte INDEX_BOOT_SCENE = 0;
	private static final byte INDEX_INTRO_SCENE = 1;
	private static final byte INDEX_CREDIT_SCENE = 2;
	private static final byte INDEX_PLAY_SCENE = 3;

	private final Rendering2D renderer;
	private final GameScene2D[] scenes;

	public GameSceneConfiguration(Rendering2D renderer, GameScene2D... scenes) {
		checkNotNull(renderer);
		checkNotNull(scenes);
		if (scenes.length != 7) {
			throw new IllegalArgumentException(
					"Scene configuration must provide 7 game scenes in order: boot, intro, credit, play, cut1, cut2, cut3.");
		}
		this.renderer = renderer;
		this.scenes = scenes;
	}

	public Rendering2D renderer() {
		return renderer;
	}

	public GameScene2D bootScene() {
		return scenes[INDEX_BOOT_SCENE];
	}

	public GameScene2D creditScene() {
		return scenes[INDEX_CREDIT_SCENE];
	}

	public GameScene2D introScene() {
		return scenes[INDEX_INTRO_SCENE];
	}

	public GameScene2D playScene() {
		return scenes[INDEX_PLAY_SCENE];
	}

	public GameScene2D cutScene(int cutSceneNumber) {
		return scenes[INDEX_PLAY_SCENE + cutSceneNumber];
	}
}