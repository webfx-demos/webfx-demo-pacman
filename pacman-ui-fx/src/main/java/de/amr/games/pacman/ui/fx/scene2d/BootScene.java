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

import static de.amr.games.pacman.lib.Globals.RND;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2i;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.Spritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.SpritesheetRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	private static final int TILES_X = World.TILES_X;
	private static final int TILES_Y = World.TILES_Y;
	private static final Vector2i SIZE_PX = v2i(TS * TILES_X, TS * TILES_Y);

	private final GraphicsContext ctx;
	private final WritableImage image;

	public BootScene(GameController gameController) {
		super(gameController);
		ctx = new Canvas(SIZE_PX.x(), SIZE_PX.y()).getGraphicsContext2D();
		image = new WritableImage(SIZE_PX.x(), SIZE_PX.y());
	}

	@Override
	public void init() {
		context.setScoreVisible(false);
		clearImage();
		saveImage();
	}

	private void clearImage() {
		ctx.setFill(ArcadeTheme.BLACK);
		ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	private void saveImage() {
		ctx.getCanvas().snapshot(null, image);
	}

	@Override
	public void update() {
		var timer = context.state().timer();
		if (timer.betweenSeconds(1.0, 2.0) && timer.tick() % 4 == 0) {
			produceRandomHexCodesImage();
		} else if (timer.betweenSeconds(2.0, 3.5) && timer.tick() % 4 == 0) {
			produceRandomSpriteImage();
		} else if (timer.atSecond(3.5)) {
			produceGridImage();
		} else if (timer.atSecond(4.0)) {
			context.gameController().terminateCurrentState();
		}
	}

	@Override
	public void drawScene(GraphicsContext g) {
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
		g.drawImage(image, 0, 0);
	}

	private void produceRandomHexCodesImage() {
		clearImage();
		ctx.setFill(ArcadeTheme.PALE);
		ctx.setFont(context.rendering2D().screenFont(TS));
		for (int row = 0; row < TILES_Y; ++row) {
			for (int col = 0; col < TILES_X; ++col) {
				var hexCode = Integer.toHexString(RND.nextInt(16));
				ctx.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
		saveImage();
	}

	private void produceRandomSpriteImage() {
		clearImage();
		if (context.rendering2D() instanceof SpritesheetRenderer) {
			SpritesheetRenderer ssr = (SpritesheetRenderer) context.rendering2D();
			for (int row = 0; row < TILES_Y / 2; ++row) {
				if (RND.nextInt(100) > 10) {
					var region1 = randomSquare(ssr.spritesheet());
					var region2 = randomSquare(ssr.spritesheet());
					var splitX = TILES_X / 8 + RND.nextInt(TILES_X / 4);
					for (int col = 0; col < TILES_X / 2; ++col) {
						var region = col < splitX ? region1 : region2;
						ssr.drawSprite(ctx, region, region.getWidth() * col, region.getHeight() * row);
					}
				}
			}
		}
		saveImage();
	}

	private Rectangle2D randomSquare(Spritesheet ss) {
		var source = ss.source();
		var raster = ss.raster();
		double x = RND.nextDouble() * (source.getWidth() - raster);
		double y = RND.nextDouble() * (source.getHeight() - raster);
		return new Rectangle2D(x, y, raster, raster);
	}

	private void produceGridImage() {
		clearImage();
		var cellSize = 16;
		var numRows = TILES_Y / 2;
		var numCols = TILES_X / 2;
		ctx.setStroke(ArcadeTheme.PALE);
		ctx.setLineWidth(2.0);
		for (int row = 0; row <= numRows; ++row) {
			ctx.strokeLine(0, row * cellSize, SIZE_PX.x(), row * cellSize);
		}
		for (int col = 0; col <= numCols; ++col) {
			ctx.strokeLine(col * cellSize, 0, col * cellSize, SIZE_PX.y());
		}
		saveImage();
	}
}