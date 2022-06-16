/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.controller.pacman.IntroController.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends GameScene2D {

	private IntroController sceneController;
	private IntroController.Context $;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new IntroController(gameController);
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(IntroController.State.START);
		creditVisible = false;
		$.pacMan.setAnimations(new PacAnimations(r2D));
		Stream.of($.ghosts).forEach(ghost -> ghost.setAnimations(new GhostAnimations(ghost.id, r2D)));
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			gameController.state().addCredit(game);
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			gameController.state().requestGame(game);
		} else if (Keyboard.pressed(KeyCode.V)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.Z)) {
			// TODO remove after testing
			game.intermissionTestNumber = 1;
			gameController.state().startIntermissionTest(game);
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		updateAnimations();
		creditVisible = $.creditVisible;
	}

	private void updateAnimations() {
		// TODO this is not elegant but works
		if (sceneController.state() == State.CHASING_GHOSTS) {
			for (var ghost : $.ghosts) {
				ghost.animations().ifPresent(animations -> {
					if (ghost.state == GhostState.DEAD && ghost.killIndex != -1) {
						animations.select("value");
					} else {
						animations.select("blue");
						if (ghost.velocity.length() == 0) {
							animations.byName("blue").stop();
						}
					}
				});
			}
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		var time = sceneController.state().timer().tick();

		r2D.drawScore(g, game.scores.gameScore);
		r2D.drawScore(g, game.scores.highScore);
		if (creditVisible) {
			r2D.drawCredit(g, game.credit());
		}

		switch (sceneController.state()) {
		case START, PRESENTING_GHOSTS -> drawGallery(g);
		case SHOWING_POINTS -> {
			drawGallery(g);
			drawPoints(g);
			if (time > sec_to_ticks(1)) {
				drawBlinkingEnergizer(g);
				r2D.drawCopyright(g, 32);
			}
		}
		case CHASING_PAC -> {
			drawGallery(g);
			drawPoints(g);
			r2D.drawCopyright(g, 32);
			drawBlinkingEnergizer(g);
			drawGuys(g, flutter(time));
		}
		case CHASING_GHOSTS -> {
			drawGallery(g);
			drawPoints(g);
			r2D.drawCopyright(g, 32);
			drawGuys(g, 0);
		}
		case READY_TO_PLAY -> {
			drawGallery(g);
			drawPoints(g);
			r2D.drawCopyright(g, 32);
			drawGuys(g, 0);
		}
		}
	}

	// TODO inspect further in MAME what's really going on
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery(GraphicsContext g) {
		if ($.titleVisible) {
			g.setFill(Color.WHITE);
			g.setFont(r2D.getArcadeFont());
			g.fillText("CHARACTER", t($.left + 3), t(6));
			g.fillText("/", t($.left + 13), t(6));
			g.fillText("NICKNAME", t($.left + 15), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if ($.pictureVisible[id]) {
				int tileY = 7 + 3 * id;
				r2D.drawSpriteCenteredOverBox(g, r2D.getGhostSprite(id, Direction.RIGHT), t($.left) + 4, t(tileY));
				if ($.characterVisible[id]) {
					g.setFill(r2D.getGhostColor(id));
					g.setFont(r2D.getArcadeFont());
					g.fillText("-" + $.characters[id], t($.left + 3), t(tileY + 1));
				}
				if ($.nicknameVisible[id]) {
					g.setFill(r2D.getGhostColor(id));
					g.setFont(r2D.getArcadeFont());
					g.fillText("\"" + $.nicknames[id] + "\"", t($.left + 14), t(tileY + 1));
				}
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		if ($.blinking.frame()) {
			g.setFill(r2D.getFoodColor(1));
			g.fillOval(t($.left), t(20), TS, TS);
		}
	}

	private void drawGuys(GraphicsContext g, int offset_x) {
		if (offset_x == 0) {
			r2D.drawGhosts(g, $.ghosts);
		} else {
			r2D.drawGhost(g, $.ghosts[0]);
			g.save();
			g.translate(offset_x, 0);
			r2D.drawGhost(g, $.ghosts[1]);
			r2D.drawGhost(g, $.ghosts[2]);
			g.restore();
			r2D.drawGhost(g, $.ghosts[3]);
		}
		r2D.drawPac(g, $.pacMan);
	}

	private void drawPoints(GraphicsContext g) {
		int tileX = $.left + 6, tileY = 25;
		g.setFill(r2D.getFoodColor(1));
		g.fillRect(t(tileX) + 4, t(tileY - 1) + 4, 2, 2);
		if ($.blinking.frame()) {
			g.fillOval(t(tileX), t(tileY + 1), TS, TS);
		}
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(r2D.getArcadeFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}