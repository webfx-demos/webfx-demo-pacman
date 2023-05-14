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

package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2i;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.anim.AnimationByDirection;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.ArcadeTheme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

/**
 * This test class is used to check if the Rendering2D interface is adequate.
 * 
 * @author Armin Reichert
 */
public class PacManTestRenderer implements Rendering2D {

	static final MazeColoring MAZE_COLORS = new MazeColoring(Color.rgb(254, 189, 180), Color.rgb(33, 33, 255).darker(),
			Color.rgb(33, 33, 255).brighter(), Color.rgb(252, 181, 255));

	@Override
	public Font screenFont(double size) {
		return AppRes.Fonts.font(AppRes.Fonts.arcade, size);
	}

	@Override
	public MazeColoring mazeColors(int mazeNumber) {
		return MAZE_COLORS;
	}

	@Override
	public GhostColoring ghostColors(int ghostID) {
		return ArcadeTheme.GHOST_COLORING[ghostID];
	}

	@Override
	public AnimationMap createPacAnimations(Pac pac) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
		map.put(GameModel.AK_PAC_DYING, createPacDyingAnimation());
		map.put(GameModel.AK_PAC_MUNCHING, createPacMunchingAnimation(pac));
		map.select(GameModel.AK_PAC_MUNCHING);
		return map;
	}

	private AnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new AnimationByDirection(pac::moveDir);
		var animation = new SimpleAnimation<>(0, 0, 90, 90, 120, 120, 90, 90);
		animation.setFrameDuration(1);
		animation.repeatForever();
		for (var dir : Direction.values()) {
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SimpleAnimation<Integer> createPacDyingAnimation() {
		var animation = new SimpleAnimation<>(45, 60, 75, 90, 135, 180, 225, 270, 315, 360);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public AnimationMap createGhostAnimations(Ghost ghost) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
//		map.put(AnimKeys.GHOST_COLOR, createGhostColorAnimation(ghost));
//		map.put(AnimKeys.GHOST_BLUE, createGhostBlueAnimation());
//		map.put(AnimKeys.GHOST_EYES, createGhostEyesAnimation(ghost));
		map.put(GameModel.AK_GHOST_FLASHING, new Pulse(6, true));
//		map.put(AnimKeys.GHOST_VALUE, createGhostValueSpriteList());
		return map;
	}

	@Override
	public AnimationMap createWorldAnimations(World world) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
		map.put(GameModel.AK_MAZE_ENERGIZER_BLINKING, new Pulse(10, true));
		map.put(GameModel.AK_MAZE_FLASHING, new Pulse(10, true));
		return null;
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		if (pac.isVisible()) {
			if (pac.isAnimationSelected(GameModel.AK_PAC_MUNCHING)) {
				drawPacMunching(g, pac, pac.animation().get());
			} else if (pac.isAnimationSelected(GameModel.AK_PAC_DYING)) {
				drawPacDying(g, pac, pac.animation().get());
			}
		}
	}

	private void drawPacMunching(GraphicsContext g, Pac pac, Animated munching) {
		int radius = 7;
		float x = pac.position().x() - radius / 2;
		float y = pac.position().y() - radius / 2;
		int openess = (int) munching.frame();
		int start = openess / 2;
		int fromAngle = 0;
		switch (pac.moveDir()) {
			case RIGHT: fromAngle = start; break;
			case UP: fromAngle = start + 90; break;
			case LEFT: fromAngle = start + 180; break;
			case DOWN: fromAngle = start + 270;
		}
		g.setFill(Color.YELLOW);
		g.fillArc(x, y, 2 * radius, 2 * radius, fromAngle, 360 - openess, ArcType.ROUND);
	}

	private void drawPacDying(GraphicsContext g, Pac pac, Animated dying) {
		int radius = 7;
		float x = pac.position().x() - radius / 2;
		float y = pac.position().y() - radius / 2;
		int openess = (int) (dying.isRunning() ? dying.frame() : 360);
		int start = openess / 2;
		int fromAngle = start + 90;
		g.setFill(Color.YELLOW);
		g.fillArc(x, y, 2 * radius, 2 * radius, fromAngle, 360 - openess, ArcType.ROUND);
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost) {
		if (!ghost.isVisible()) {
			return;
		}
		switch (ghost.state()) {
			case EATEN: {
			if (ghost.killedIndex() >= 0) {
				drawGhostBounty(g, ghost);
			} else {
				drawGhostEyes(g, ghost);
			}
			return;
		}
			case RETURNING_TO_HOUSE:
			case ENTERING_HOUSE: {
			drawGhostEyes(g, ghost);
			break;
		}
			case FRIGHTENED: {
			var color = Color.BLUE;
			var flashing = ghost.animation();
			if (flashing.isPresent() && (boolean) flashing.get().frame()) {
				color = Color.WHITE;
			}
			drawGhostBody(g, ghost, color);
			break;
		}
			default: {
			drawGhostBody(g, ghost, ghostColors(ghost.id()).dress());
		}
		}
	}

	public void drawGhostBody(GraphicsContext g, Ghost ghost, Color color) {
		g.setFill(color);
		g.fillRoundRect(ghost.position().x() - 2, ghost.position().y() - 4, 12, 16, 6, 8);
	}

	public void drawGhostBounty(GraphicsContext g, Ghost ghost) {
		g.setStroke(ArcadeTheme.CYAN);
		g.setFont(Font.font("Sans", 10));
		String text;
		switch (ghost.killedIndex()) {
			case 0: text = "200"; break;
			case 1: text = "400"; break;
			case 2: text = "800"; break;
			case 3: text = "1600"; break;
			default: text = "???";
		};
		g.strokeText(text, ghost.position().x() - 4, ghost.position().y() + 6);
	}

	public void drawGhostEyes(GraphicsContext g, Ghost ghost) {
		var color = Color.WHITE;
		g.setStroke(color);
		g.strokeOval(ghost.position().x() - 3, ghost.position().y() + 2, 4, 4);
		g.strokeOval(ghost.position().x() + 3, ghost.position().y() + 2, 4, 4);
	}

	@Override
	public void drawGhostFacingRight(GraphicsContext g, int id, int x, int y) {
		var color = ghostColors(id).dress();
		g.setFill(color);
		g.fillRect(x - 2, y - 4, 12, 16);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		var x = bonus.entity().position().x();
		var y = bonus.entity().position().y() + 8;
		switch (bonus.state()) {
			case Bonus.STATE_EDIBLE: Rendering2D.drawText(g, "Bonus", Color.YELLOW, AppRes.Fonts.arcade, x - 20, y); break;
			case Bonus.STATE_EATEN: Rendering2D.drawText(g, bonus.points() + "", Color.RED, AppRes.Fonts.arcade, x - 8, y); break;
			default: {
		}
		}
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelCounter) {
		levelNumber.ifPresent(number -> {
			Rendering2D.drawText(g, "Level " + number, Color.WHITE, AppRes.Fonts.arcade, 18 * TS, 36 * TS - 2);
		});
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = TS * (2);
		int y = TS * (World.TILES_Y - 1);
		int maxLives = 5;
		int size = 14;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			g.setFill(Color.YELLOW);
			g.fillOval(x + TS * (2 * i) - 7, y - 7, size, size);
		}
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world) {
		boolean flash = false;
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			flash = (boolean) flashingAnimation.get().frame();
			drawWalls(g, world, flash);
			return;
		}
		var energizerBlinking = world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING);
		boolean on = energizerBlinking.isPresent() && (boolean) energizerBlinking.get().frame();
		drawWalls(g, world, false);
		drawFood(g, mazeNumber, world, !on);
	}

	private void drawWalls(GraphicsContext g, World world, boolean flash) {
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				var tile = v2i(col, row);
				if (world.isWall(tile)) {
					g.setFill(flash ? Color.WHITE : Color.SADDLEBROWN);
					g.fillRect(tile.x() * TS, tile.y() * TS, TS, TS);
				}
			}
		}
	}

	private void drawFood(GraphicsContext g, int mazeNumber, World world, boolean energizersHidden) {
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				var tile = v2i(col, row);
				if (world.containsFood(tile)) {
					g.setFill(mazeColors(mazeNumber).foodColor());
					if (world.isEnergizerTile(tile)) {
						if (!energizersHidden) {
							g.fillOval(tile.x() * TS, tile.y() * TS, TS, TS);
						}
					} else {
						g.fillRect(tile.x() * TS + 3, tile.y() * TS + 3, 2, 2);
					}
				}
			}
		}
	}
}