/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 2D representation of the maze. Implements the flashing animation played on the end of each level.
 * 
 * @author Armin Reichert
 */
public class Maze2D implements Renderable2D {

	private final Rendering2D rendering;
	private final int x;
	private final int y;

	private GameModel game;
	private List<Energizer2D> energizers2D;
	private Timeline flashingAnimation;
	private boolean flashing;
	private TimedSequence<Boolean> energizerAnimation = TimedSequence.pulse().frameDuration(10);

	public Maze2D(GameModel game, V2i leftUpperCorner, Rendering2D rendering) {
		this.rendering = rendering;
		x = t(leftUpperCorner.x);
		y = t(leftUpperCorner.y);
		flashingAnimation = new Timeline(new KeyFrame(Duration.millis(150), e -> flashing = !flashing));
		onGameChanged(game);
	}

	public void onGameChanged(GameModel game) {
		this.game = game;
		energizers2D = game.world.energizerTiles().map(energizerTile -> {
			Energizer2D energizer2D = new Energizer2D();
			energizer2D.x = t(energizerTile.x);
			energizer2D.y = t(energizerTile.y);
			energizer2D.animation = energizerAnimation;
			return energizer2D;
		}).collect(Collectors.toList());
		flashingAnimation.setCycleCount(2 * game.numFlashes);
	}

	public boolean isFlashing() {
		return flashingAnimation.getStatus() == Status.RUNNING;
	}

	public Timeline getFlashingAnimation() {
		return flashingAnimation;
	}

	public TimedSequence<Boolean> getEnergizerAnimation() {
		return energizerAnimation;
	}

	@Override
	public void render(GraphicsContext g) {
		if (flashingAnimation.getStatus() == Status.RUNNING) {
			if (flashing) {
				rendering.renderMazeFlashing(g, game.mazeNumber, x, y);
			} else {
				rendering.renderMazeEmpty(g, game.mazeNumber, x, y);
			}
		} else {
			rendering.renderMazeFull(g, game.mazeNumber, x, y);
			energizers2D.forEach(energizer2D -> energizer2D.render(g));
			energizerAnimation.animate();
			g.setFill(Color.BLACK);
			game.world.tiles().filter(game::isFoodRemoved).forEach(emptyFoodTile -> {
				g.fillRect(t(emptyFoodTile.x), t(emptyFoodTile.y), TS, TS);
			});
		}
	}
}