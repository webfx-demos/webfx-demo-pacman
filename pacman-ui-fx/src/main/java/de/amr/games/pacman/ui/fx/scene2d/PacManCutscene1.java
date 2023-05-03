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

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2i;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PacManCutscene1 extends GameScene2D {
	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;

	public PacManCutscene1(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.25f);
		pac.show();

		// TODO make this work for all renderers
		if (context.rendering2D() instanceof PacManGameRenderer) {
			PacManGameRenderer r = (PacManGameRenderer) context.rendering2D();
			var pacAnimations = r.createPacAnimations(pac);
			pacAnimations.put(GameModel.AK_PAC_BIG, r.createBigPacManMunchingAnimation());
			pac.setAnimations(pacAnimations);
			pac.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
		}

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(32, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(1.3f);
		blinky.show();

		var blinkyAnimations = context.rendering2D().createGhostAnimations(blinky);
		blinky.setAnimations(blinkyAnimations);
		blinkyAnimations.selectedAnimation().ifPresent(Animated::restart);
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent("start_intermission_1");
			}
			return;
		}

		if (context.state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
			case 260: {
			blinky.placeAtTile(v2i(-2, 20), 4, 0);
			blinky.setMoveAndWishDir(Direction.RIGHT);
			blinky.setPixelSpeed(0.75f);
			blinky.animations().ifPresent(animations -> animations.selectAndRestart(GameModel.AK_GHOST_BLUE));
			break;
		}
			case 400: {
			pac.placeAtTile(v2i(-3, 19), 0, 0);
			pac.setMoveDir(Direction.RIGHT);
			pac.animations().ifPresent(animations -> animations.selectAndRestart(GameModel.AK_PAC_BIG));
			break;
		}
			case 632: {
			context.state().timer().expire();
			break;
		}
			default: {
			pac.move();
			pac.animate();
			blinky.move();
			blinky.animate();
			break;
		}
		}
	}

	@Override
	public void drawScene(GraphicsContext g) {
		context.rendering2D().drawPac(g, pac);
		context.rendering2D().drawGhost(g, blinky);
		drawLevelCounter(g);
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		if (Env.showDebugInfoPy.get()) {
			g.setFont(context.rendering2D().screenFont(TS));
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait " + initialDelay, TS * (1), TS * (5));
			} else {
				g.fillText("Frame " + frame, TS * (1), TS * (5));
			}
		}
	}
}