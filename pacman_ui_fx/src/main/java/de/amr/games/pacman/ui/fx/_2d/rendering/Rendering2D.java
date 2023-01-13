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
package de.amr.games.pacman.ui.fx._2d.rendering;

import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.FixedEntityAnimation;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for Pac-Man and Ms. Pac-Man (spritesheet based) rendering.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	Spritesheet spritesheet();

	Font arcadeFont(double size);

	Color ghostColor(int ghostID);

	Color mazeFoodColor(int mazeNumber);

	Color mazeTopColor(int mazeNumber);

	Color mazeSideColor(int mazeNumber);

	Color ghostHouseDoorColor();

	// Sprites

	Rectangle2D ghostSprite(int ghostID, Direction dir);

	Rectangle2D lifeSprite();

	Rectangle2D bonusSymbolSprite(int symbol);

	Rectangle2D bonusValueSprite(int symbol);

	// Animations

	EntityAnimationMap<AnimKeys> createPacAnimations(Pac pac);

	EntityAnimationByDirection createPacMunchingAnimation(Pac pac);

	SingleEntityAnimation<Rectangle2D> createPacDyingAnimation();

	EntityAnimationMap<AnimKeys> createGhostAnimations(Ghost ghost);

	EntityAnimationByDirection createGhostColorAnimation(Ghost ghost);

	SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation();

	SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation();

	EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost);

	SingleEntityAnimation<Boolean> createMazeFlashingAnimation();

	FixedEntityAnimation<Rectangle2D> createGhostValueList();

	// Drawing

	void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y);

	/**
	 * Draws sprite (region) using spritesheet.
	 * 
	 * @param g      graphics context
	 * @param sprite sprite (region in spritesheet), may be null
	 * @param x      left upper corner x
	 * @param y      left upper corner y
	 */
	void drawSprite(GraphicsContext g, Rectangle2D region, double x, double y);

	/**
	 * Draws the sprite defined by the given spritesheet region centered of the the one square tile box with left upper
	 * corner defined by the given coordinates.
	 * 
	 * @param g      graphics context
	 * @param region sprite region in spritesheet, my be null
	 * @param x      left upper corner of the box (one square tile)
	 * @param y      left upper corner of the box
	 */
	void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D region, double x, double y);

	/**
	 * Draws the entity's sprite centered over the entity's collision box. The collision box is a square with left upper
	 * corner defined by the entity position and side length of one tile size. Respects the current visibility of the
	 * entity.
	 * 
	 * @param g      graphics context
	 * @param entity entity
	 * @param s      entity sprite (region in spritesheet), may be null
	 */
	void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D region);

	void drawPac(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawCopyright(GraphicsContext g, int tileY);

	void drawLevelCounter(GraphicsContext g, Iterable<Byte> levelCounter);

	void drawLivesCounter(GraphicsContext g, int numLivesDisplayed);

	void drawHUD(GraphicsContext g, GameModel game, boolean creditVisible);

	void drawEmptyMaze(GraphicsContext g, int x, int y, int mazeNumber, boolean flash);

	void drawFilledMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden);

	void drawGameReadyMessage(GraphicsContext g);

	void drawGameOverMessage(GraphicsContext g);
}