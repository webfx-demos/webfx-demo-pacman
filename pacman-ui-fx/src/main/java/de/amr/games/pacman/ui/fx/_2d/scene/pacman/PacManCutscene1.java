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

package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
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
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.25f);
		pac.show();

		// TODO make this work for all renderers
		if (context.r2D() instanceof PacManGameRenderer r) {
			var pacAnimations = r.createPacAnimations(pac);
			pacAnimations.put(PacManGame.AK_PAC_BIG, r.createBigPacManMunchingAnimation());
			pac.setAnimations(pacAnimations);
			pac.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
		}

		blinky = new Ghost(Ghost.ID_RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(32, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(1.3f);
		blinky.show();

		var blinkyAnimations = context.r2D().createGhostAnimations(blinky);
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
		case 260 -> {
			blinky.placeAtTile(v2i(-2, 20), 4, 0);
			blinky.setMoveAndWishDir(Direction.RIGHT);
			blinky.setPixelSpeed(0.75f);
			blinky.animations().ifPresent(animations -> animations.selectAndRestart(GameModel.AK_GHOST_BLUE));
		}
		case 400 -> {
			pac.placeAtTile(v2i(-3, 19), 0, 0);
			pac.setMoveDir(Direction.RIGHT);
			pac.animations().ifPresent(animations -> animations.selectAndRestart(PacManGame.AK_PAC_BIG));
		}
		case 632 -> {
			context.state().timer().expire();
		}
		default -> {
			pac.move();
			pac.animate();
			blinky.move();
			blinky.animate();
		}
		}
	}

	@Override
	public void drawSceneContent() {
		context.r2D().drawPac(g, pac);
		context.r2D().drawGhost(g, blinky);
		context.r2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	@Override
	protected void drawOverlayPaneContent() {
		if (Env.showDebugInfoPy.get()) {
			g.setFont(context.r2D().screenFont(TS));
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), t(1), t(5));
			} else {
				g.fillText("Frame %d".formatted(frame), t(1), t(5));
			}
		}
	}
}