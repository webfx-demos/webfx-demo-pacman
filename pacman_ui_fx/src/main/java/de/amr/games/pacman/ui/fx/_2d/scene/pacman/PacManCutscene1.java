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

import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
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

	@Override
	public void init() {
		var renderer = (RendererPacManGame) ctx.r2D();
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.setAnimationSet(renderer.createPacAnimationSet(pac));
		var bigPacAnimation = renderer.createBigPacManMunchingAnimation();
		pac.animationSet().ifPresent(animations -> animations.put(AnimKeys.PAC_BIG, bigPacAnimation));
		pac.selectAndRunAnimation(AnimKeys.PAC_MUNCHING);
		pac.placeAtTile(v(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setAbsSpeed(1.25);
		pac.show();

		blinky = new Ghost(Ghost.RED_GHOST, "Blinky");
		blinky.setAnimationSet(renderer.createGhostAnimationSet(blinky));
		blinky.animationSet().ifPresent(animations -> {
			animations.select(AnimKeys.GHOST_COLOR);
			animations.animation(AnimKeys.GHOST_COLOR).get().restart();
		});
		blinky.placeAtTile(v(32, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setAbsSpeed(1.3);
		blinky.show();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			return;
		}
		++frame;
		if (frame == 0) {
			ctx.sounds().loop(GameSound.INTERMISSION_1, 2);
		} else if (frame == 260) {
			blinky.placeAtTile(v(-2, 20), 4, 0);
			blinky.setMoveAndWishDir(Direction.RIGHT);
			blinky.animationSet().ifPresent(animations -> {
				animations.select(AnimKeys.GHOST_BLUE);
				animations.selectedAnimation().get().restart();
			});
			blinky.setAbsSpeed(0.75);
		} else if (frame == 400) {
			pac.placeAtTile(v(-3, 19), 0, 0);
			pac.setMoveDir(Direction.RIGHT);
			pac.animationSet().ifPresent(animations -> {
				animations.select(AnimKeys.PAC_BIG);
				animations.selectedAnimation().get().restart();
			});
		} else if (frame == 632) {
			ctx.state().timer().expire();
			return;
		}
		pac.move();
		pac.updateAnimation();
		blinky.move();
		blinky.updateAnimation();
	}

	@Override
	public void draw(GraphicsContext g) {
		if (Env.showDebugInfoPy.get()) {
			g.setFont(ctx.r2D().arcadeFont());
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), t(1), t(5));
			} else {
				g.fillText("Frame %d".formatted(frame), t(1), t(5));
			}
		}
		ctx.r2D().drawPac(g, pac);
		ctx.r2D().drawGhost(g, blinky);
	}
}