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

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.AppRes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for all 2D renderers.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	Font screenFont(double size);

	default PacManColoring pacManColors() {
		return AppRes.ArcadeTheme.PACMAN_COLORING;
	}

	default MsPacManColoring msPacManColors() {
		return AppRes.ArcadeTheme.MS_PACMAN_COLORING;
	}

	default GhostColoring[] ghostColors() {
		return AppRes.ArcadeTheme.GHOST_COLORING;
	}

	default GhostColoring ghostColors(int ghostID) {
		return AppRes.ArcadeTheme.GHOST_COLORING[ghostID];
	}

	MazeColoring mazeColors(int mazeNumber);

	// Animations

	AnimationMap createPacAnimations(Pac pac);

	AnimationMap createGhostAnimations(Ghost ghost);

	AnimationMap createWorldAnimations(World world);

	// Drawing

	public static void drawTileStructure(GraphicsContext g, int tilesX, int tilesY) {
		g.save();
		g.translate(0.5, 0.5);
		g.setStroke(AppRes.ArcadeTheme.PALE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, TS * (row), tilesX * TS, TS * (row));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(TS * (col), 0, TS * (col), tilesY * TS);
		}
		g.restore();
	}

	public static void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	default void fillCanvas(GraphicsContext g, Color color) {
		g.setFill(AppRes.ArcadeTheme.BLACK);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	default void hideTileContent(GraphicsContext g, Vector2i tile) {
		g.setFill(AppRes.ArcadeTheme.BLACK);
		g.fillRect(TS * (tile.x()), TS * (tile.y()), TS, TS);
	}

	default void drawScore(GraphicsContext g, Score score, String title, Font font, Color color, double x, double y) {
		drawText(g, title, color, font, x, y);
		var pointsText = " " + String.valueOf(score.points());
		drawText(g, pointsText, color, font, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L" + score.levelNumber(), color, font, x + TS * (8), y + TS + 1);
		}
	}

	void drawPac(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost);

	void drawGhostFacingRight(GraphicsContext g, int id, int x, int y);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelSymbols);

	void drawLivesCounter(GraphicsContext g, int numLivesDisplayed);

	void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world);
}