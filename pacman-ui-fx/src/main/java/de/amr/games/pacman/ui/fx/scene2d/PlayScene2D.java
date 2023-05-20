/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.app.GameApp;
import de.amr.games.pacman.ui.fx.input.GestureHandler;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private GestureHandler gestureHandler;
	private Rectangle touchPad = new Rectangle(80, 50);

	public PlayScene2D(GameController gameController) {
		super(gameController);
		// Dragging the mouse from some point in the ghosthouse to any direction steers Pac-Man
		touchPad.setTranslateX(0.5 * (GameScene2D.WIDTH - touchPad.getWidth()));
		touchPad.setTranslateY(0.5 * (GameScene2D.HEIGHT - touchPad.getHeight()) - 4);
		touchPad.setFill(Color.gray(0.25, 0.1));
		overlay.getChildren().add(touchPad);
		gestureHandler = new GestureHandler(touchPad);
		gestureHandler.setOnDirectionRecognized(dir -> {
			context.game().level().ifPresent(level -> {
				level.pac().setWishDir(dir);
			});
		});
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(GameApp.KEY_ADD_CREDIT) || Keyboard.pressed(GameApp.KEY_ADD_CREDIT_NUMPAD)) {
			if (!context().hasCredit()) {
				GameApp.app.addCredit();
			}
		} else if (Keyboard.pressed(GameApp.KEY_CHEAT_EAT_ALL)) {
			GameApp.app.cheatEatAllPellets();
		} else if (Keyboard.pressed(GameApp.KEY_CHEAT_ADD_LIVES)) {
			GameApp.app.cheatAddLives(3);
		} else if (Keyboard.pressed(GameApp.KEY_CHEAT_NEXT_LEVEL)) {
			GameApp.app.cheatEnterNextLevel();
		} else if (Keyboard.pressed(GameApp.KEY_CHEAT_KILL_GHOSTS)) {
			GameApp.app.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void init() {
		context.setCreditVisible(!context.hasCredit());
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		if (context.state() == GameState.GAME_OVER) {
			context.setCreditVisible(true);
		}
		context.level().ifPresent(this::updateSound);
	}

	@Override
	public void end() {
		context.sounds().stopAll();
	}

	@Override
	public void drawScene(GraphicsContext g) {
		context.level().ifPresent(level -> {
			var r = context.rendering2D();
			var mazeNumber = level.game().mazeNumber(level.number());
			r.drawMaze(g, 0, TS * (3), mazeNumber, level.world());
			if (context.state() == GameState.LEVEL_TEST) {
				drawText(g, "TEST    " + level.number(), ArcadeTheme.YELLOW, r.screenFont(TS), TS * (8) + 4,
						TS * (21));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				drawText(g, "GAME  OVER", ArcadeTheme.RED, r.screenFont(TS), TS * (9), TS * (21));
			} else if (context.state() == GameState.READY) {
				drawText(g, "READY!", ArcadeTheme.YELLOW, r.screenFont(TS), TS * (11), TS * (21));
			}
			level.bonusManagement().getBonus().ifPresent(bonus -> r.drawBonus(g, bonus));
			r.drawPac(g, level.pac());
			r.drawGhost(g, level.ghost(GameModel.ORANGE_GHOST));
			r.drawGhost(g, level.ghost(GameModel.CYAN_GHOST));
			r.drawGhost(g, level.ghost(GameModel.PINK_GHOST));
			r.drawGhost(g, level.ghost(GameModel.RED_GHOST));
			if (!context.isCreditVisible()) {
				// TODO get rid of this crap
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				r.drawLivesCounter(g, lives);
			}
		});
	}

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 10) {
			context.sounds().stop(AudioClipID.PACMAN_MUNCH);
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			context.sounds().ensureLoop(AudioClipID.GHOST_RETURNING, AudioClip.INDEFINITE);
		} else {
			context.sounds().stop(AudioClipID.GHOST_RETURNING);
		}
	}
}