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
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.mspacman.IntroController;
import de.amr.games.pacman.controller.mspacman.IntroController.Context;
import de.amr.games.pacman.controller.mspacman.IntroController.State;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

	private IntroController sceneController;
	private Context icc;

	@Override
	public void setContext(SceneContext sceneContext) {
		super.setContext(sceneContext);
		sceneController = new IntroController(sceneContext.gameController());
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInState(IntroController.State.START);
		var pacAnimations = ctx.r2D().createPacAnimationSet(icc.game.pac);
		pacAnimations.ensureRunning();
		icc.game.pac.setAnimationSet(pacAnimations);
		icc.game.ghosts().forEach(ghost -> {
			var ghostAnimations = ctx.r2D().createGhostAnimationSet(ghost);
			ghostAnimations.ensureRunning();
			ghost.setAnimationSet(ghostAnimations);
		});
	}

	@Override
	public void update() {
		sceneController.update();
		setCreditVisible(icc.creditVisible);
	}

	@Override
	public void end() {
		Actions.stopVoiceMessage();
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			Actions.startGame();
		} else if (Keyboard.pressed(KeyCode.V)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.Z)) {
			Actions.startCutscenesTest();
		}
	}

	@Override
	public void draw(GraphicsContext g) {
		drawTitle(g);
		drawLights(g, 32, 16);
		if (sceneController.state() == State.GHOSTS) {
			drawGhostText(g, icc.game.theGhosts[icc.ghostIndex]);
		} else if (sceneController.state() == State.MSPACMAN || sceneController.state() == State.READY_TO_PLAY) {
			drawMsPacManText(g);
		}
		for (var ghost : icc.game.theGhosts) {
			ctx.r2D().drawGhost(g, ghost);
		}
		ctx.r2D().drawPac(g, icc.game.pac);
		ctx.r2D().drawCopyright(g, 29);
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter);
	}

	private void drawTitle(GraphicsContext g) {
		ctx.r2D().drawText(g, "\"MS PAC-MAN\"", Color.ORANGE, ctx.r2D().arcadeFont(), icc.titlePosition.x(),
				icc.titlePosition.y());
	}

	private void drawGhostText(GraphicsContext g, Ghost ghost) {
		if (ghost.id == Ghost.RED_GHOST) {
			ctx.r2D().drawText(g, "WITH", Color.WHITE, ctx.r2D().arcadeFont(), icc.titlePosition.x(),
					icc.lightsTopLeft.y() + t(3));
		}
		ctx.r2D().drawText(g, ghost.name.toUpperCase(), ctx.r2D().ghostColor(ghost.id), ctx.r2D().arcadeFont(),
				t(14 - ghost.name.length() / 2), icc.lightsTopLeft.y() + t(6));
	}

	private void drawMsPacManText(GraphicsContext g) {
		ctx.r2D().drawText(g, "STARRING", Color.WHITE, ctx.r2D().arcadeFont(), icc.titlePosition.x(),
				icc.lightsTopLeft.y() + t(3));
		ctx.r2D().drawText(g, "MS PAC-MAN", Color.YELLOW, ctx.r2D().arcadeFont(), icc.titlePosition.x(),
				icc.lightsTopLeft.y() + t(6));
	}

	// TODO this is not yet accurate to the original game
	private void drawLights(GraphicsContext g, int numDotsX, int numDotsY) {
		long time = icc.lightsTimer.tick();
		int light = (int) (time / 2) % (numDotsX / 2);
		for (int dot = 0; dot < 2 * (numDotsX + numDotsY); ++dot) {
			int x = 0;
			int y = 0;
			if (dot <= numDotsX) {
				x = dot;
			} else if (dot < numDotsX + numDotsY) {
				x = numDotsX;
				y = dot - numDotsX;
			} else if (dot < 2 * numDotsX + numDotsY + 1) {
				x = 2 * numDotsX + numDotsY - dot;
				y = numDotsY;
			} else {
				y = 2 * (numDotsX + numDotsY) - dot;
			}
			g.setFill((dot + light) % (numDotsX / 2) == 0 ? Color.PINK : Color.RED);
			g.fillRect(icc.lightsTopLeft.x() + 4 * x, icc.lightsTopLeft.y() + 4 * y, 2, 2);
		}
	}
}