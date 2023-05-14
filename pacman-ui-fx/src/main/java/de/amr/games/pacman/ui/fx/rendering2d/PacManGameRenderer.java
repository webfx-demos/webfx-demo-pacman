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

import de.amr.games.pacman.lib.anim.*;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.GameApp;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Armin Reichert
 */
public class PacManGameRenderer extends SpritesheetRenderer {

	private static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	public PacManGameRenderer() {
		super(GameApp.assets.spritesheetPacManGame);
	}

	@Override
	public MazeColoring mazeColors(int mazeNumber) {
		return ArcadeTheme.PACMAN_MAZE_COLORS;
	}

	@Override
	public Rectangle2D ghostValueRegion(int index) {
		return spritesheet.tile(index, 8);
	}

	@Override
	public Rectangle2D bonusSymbolRegion(int symbol) {
		return spritesheet.tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D bonusValueRegion(int symbol) {
		if (symbol <= 3) {
			return spritesheet.tile(symbol, 9);
		}
		if (symbol == 4) {
			var region = spritesheet.tiles(4, 9, 2, 1);
			return spritesheet.region(region.getMinX(), region.getMinY(), region.getWidth() - 13, region.getHeight()); // WTF
		}
		return spritesheet.tiles(3, 5 + symbol, 3, 1);
	}

	@Override
	public AnimationMap createWorldAnimations(World world) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
		map.put(GameModel.AK_MAZE_ENERGIZER_BLINKING, new Pulse(10, true));
		map.put(GameModel.AK_MAZE_FLASHING, new Pulse(10, true));
		return map;
	}

	@Override
	public void drawGhostFacingRight(GraphicsContext g, int ghostID, int x, int y) {
		var region = spritesheet.tile(2 * DIR_ORDER.index(Direction.RIGHT), 4 + ghostID);
		drawSpriteCenteredOverBox(g, region, x, y);
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world) {
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			var flashing = (boolean) flashingAnimation.get().frame();
			g.drawImage(flashing ? GameApp.assets.flashingMazePacManGame : GameApp.assets.emptyMazePacManGame, x, y);
		} else {
			g.drawImage(GameApp.assets.fullMazePacManGame, x, y);
			world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, tile));
			var energizerBlinking = world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING);
			boolean energizerVisible = energizerBlinking.isPresent() && (boolean) energizerBlinking.get().frame();
			if (!energizerVisible) {
				world.energizerTiles().forEach(tile -> hideTileContent(g, tile));
			}
		}
	}

	@Override
	public Rectangle2D lifeSymbolRegion() {
		return spritesheet.tile(8, 1);
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
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var wide = spritesheet.tile(0, d);
			var middle = spritesheet.tile(1, d);
			var closed = spritesheet.tile(2, 0);
			var animation = new SimpleAnimation<>(closed, closed, middle, middle, wide, wide, middle, middle);
			animation.setFrameDuration(1);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SimpleAnimation<Rectangle2D> createPacDyingAnimation() {
		var animation = new SimpleAnimation<>(spritesheet.tilesRightOf(3, 0, 11));
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public AnimationMap createGhostAnimations(Ghost ghost) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
		map.put(GameModel.AK_GHOST_COLOR, createGhostColorAnimation(ghost));
		map.put(GameModel.AK_GHOST_BLUE, createGhostBlueAnimation());
		map.put(GameModel.AK_GHOST_EYES, createGhostEyesAnimation(ghost));
		map.put(GameModel.AK_GHOST_FLASHING, createGhostFlashingAnimation());
		map.put(GameModel.AK_GHOST_VALUE, createGhostValueSpriteList());
		map.select(GameModel.AK_GHOST_COLOR);
		return map;
	}

	private AnimationByDirection createGhostColorAnimation(Ghost ghost) {
		var animationByDir = new AnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var animation = new SimpleAnimation<>(spritesheet.tilesRightOf(2 * d, 4 + ghost.id(), 2));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SimpleAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SimpleAnimation<>(spritesheet.tile(8, 4), spritesheet.tile(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	private SimpleAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SimpleAnimation<>(spritesheet.tilesRightOf(8, 4, 4));
		animation.setFrameDuration(6);
		return animation;
	}

	private AnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new AnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			animationByDir.put(dir, new SimpleAnimation<>(spritesheet.tile(8 + d, 5)));
		}
		return animationByDir;
	}

	private Animated createGhostValueSpriteList() {
		return new FrameSequence<>(ghostValueRegion(0), ghostValueRegion(1), ghostValueRegion(2), ghostValueRegion(3));
	}

	// Pac-Man specific:

	public SimpleAnimation<Rectangle2D> createBigPacManMunchingAnimation() {
		var animation = new SimpleAnimation<>(spritesheet.tiles(2, 1, 2, 2), spritesheet.tiles(4, 1, 2, 2),
				spritesheet.tiles(6, 1, 2, 2));
		animation.setFrameDuration(3);
		animation.repeatForever();
		return animation;
	}

	public FrameSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return new FrameSequence<>(spritesheet.tilesRightOf(8, 6, 5));
	}

	public FrameSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return new FrameSequence<>(spritesheet.tile(8, 7), spritesheet.tile(9, 7));
	}

	public SimpleAnimation<Rectangle2D> createBlinkyPatchedAnimation() {
		var animation = new SimpleAnimation<>(spritesheet.tile(10, 7), spritesheet.tile(11, 7));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}

	public SimpleAnimation<Rectangle2D> createBlinkyNakedAnimation() {
		var animation = new SimpleAnimation<>(spritesheet.tiles(8, 8, 2, 1), spritesheet.tiles(10, 8, 2, 1));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}
}