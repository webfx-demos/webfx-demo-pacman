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
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.GameLoop;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Armin Reichert
 */
public class ActionContext {

	GameLoop gameLoop; GameController gameController;
	Supplier<GameScene> currentGameSceneSupplier; FlashMessageView flashMessageView;

	public ActionContext(GameLoop gameLoop, GameController gameController, Supplier<GameScene> currentGameSceneSupplier, FlashMessageView flashMessageView) {
		this.gameLoop = gameLoop;
		this.gameController = gameController;
		this.currentGameSceneSupplier = currentGameSceneSupplier;
		this.flashMessageView = flashMessageView;
	}

	public GameLoop gameLoop() {
		return gameLoop;
	}

	public GameController gameController() {
		return gameController;
	}

	public Supplier<GameScene> currentGameSceneSupplier() {
		return currentGameSceneSupplier;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ActionContext that = (ActionContext) o;

		if (!Objects.equals(gameLoop, that.gameLoop)) return false;
		if (!Objects.equals(gameController, that.gameController))
			return false;
		if (!Objects.equals(currentGameSceneSupplier, that.currentGameSceneSupplier))
			return false;
		return Objects.equals(flashMessageView, that.flashMessageView);
	}

	@Override
	public int hashCode() {
		int result = gameLoop != null ? gameLoop.hashCode() : 0;
		result = 31 * result + (gameController != null ? gameController.hashCode() : 0);
		result = 31 * result + (currentGameSceneSupplier != null ? currentGameSceneSupplier.hashCode() : 0);
		result = 31 * result + (flashMessageView != null ? flashMessageView.hashCode() : 0);
		return result;
	}

	public GameModel game() {
		return gameController.game();
	}

	public GameState gameState() {
		return gameController.state();
	}

	public GameScene currentGameScene() {
		return currentGameSceneSupplier.get();
	}
}
