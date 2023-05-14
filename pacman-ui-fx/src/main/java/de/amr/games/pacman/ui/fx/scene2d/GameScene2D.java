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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.ArcadeTheme;
import de.amr.games.pacman.ui.fx.app.PacManGameAppFX;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;

/**
 * Base class of all 2D scenes. Each 2D scene has its own canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final float WIDTH = World.TILES_X * TS;
	private static final float HEIGHT = World.TILES_Y * TS;
	private static final float ASPECT_RATIO = WIDTH / HEIGHT;

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected final GameSceneContext context;
	protected final BorderPane root;
	protected final StackPane layers = new StackPane();
	protected final Canvas canvas = new Canvas();
	protected final Pane overlay = new BorderPane();
	protected final VBox helpRoot = new VBox();
	protected ImageView helpButton;

	protected GameScene2D(GameController gameController) {
		checkNotNull(gameController);
		context = new GameSceneContext(gameController);

		root = new BorderPane();
		root.heightProperty().addListener((py, ov, nv) -> {
			double scaling = nv.doubleValue() / HEIGHT;
			canvas.setScaleX(scaling);
			canvas.setScaleY(scaling);
			// don't ask me why this works but setScaleX/Y doesn't
			overlay.getTransforms().setAll(new Scale(scaling,scaling));
		});
		root.setScaleX(0.98);
		root.setScaleY(0.98);

		canvas.setWidth(WIDTH);
		canvas.setHeight(HEIGHT);

		helpRoot.setTranslateX(16);
		helpRoot.setTranslateY(16);

		// TODO: Graphic button rendering is broken in GWT
		setHelpButtonStyle(GameVariant.PACMAN);

		overlay.getChildren().add(helpRoot);

		layers.getChildren().addAll(canvas, overlay);
		root.setCenter(layers);

		infoVisiblePy.bind(PacManGameAppFX.showDebugInfoPy);
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public Node root() {
		return root;
	}

	public VBox helpRoot() {
		return helpRoot;
	}

	public ImageView helpButton() {
		return helpButton;
	}

	public void setHelpButtonStyle(GameVariant variant) {
		if (helpButton != null) {
			overlay.getChildren().remove(helpButton);
		}
		helpButton = new ImageView(variant == GameVariant.MS_PACMAN
			? AppRes.Graphics.helpIconMsPacManGame
			: AppRes.Graphics.helpIconPacManGame);
		helpButton.setTranslateX(4);
		helpButton.setTranslateY(4);
		helpButton.setFitHeight(12);
		helpButton.setFitWidth(12);
		//TODO seems like mouse click is ignored over transparent color pixels?
		helpButton.setOnMouseClicked(e -> Actions.showHelp());
		overlay.getChildren().add(helpButton);
	}

	@Override
	public void onEmbedIntoParentScene(Scene parentScene) {
		resize(parentScene.getHeight());
	}

	@Override
	public void onParentSceneResize(Scene parentScene) {
		resize(parentScene.getHeight());
	}

	@Override
	public boolean is3D() {
		return false;
	}

	/**
	 * Resizes the game scene to the given height, keeping the aspect ratio.
	 * 
	 * @param height new game scene height
	 */
	public void resize(double height) {
		if (height <= 0) {
			throw new IllegalArgumentException("Scene height must be positive");
		}
		var width = ASPECT_RATIO * height;
		root.setMinSize(width, height);
		root.setMaxSize(width, height);
	}

	@Override
	public void render() {
		var g = canvas.getGraphicsContext2D();
		var r = context.rendering2D();

		double w = canvas.getWidth();
		double h = canvas.getHeight();
		g.setFill(AppRes.Graphics.wallpaperColor);
		g.fillRect(0, 0, w, h);
		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, w, h, 20, 20);

		var color = ArcadeTheme.PALE;
		var font = r.screenFont(TS);
		if (context.isScoreVisible()) {
			context.game().score().ifPresent(score -> r.drawScore(g, score, "SCORE", font, color, TS*3, TS));
			context.game().highScore().ifPresent(score -> r.drawScore(g, score, "HIGH SCORE", font, color, TS * 16, TS));
			if (context.isCreditVisible()) {
				Rendering2D.drawText(g, "CREDIT " + context.game().credit(), color, font,TS * 2, TS * 36 - 1);
			}
		}
		drawScene(g);
		if (infoVisiblePy.get()) {
			drawInfo(g);
		}
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 * 
	 * @param g graphics context
	 */
	protected abstract void drawScene(GraphicsContext g);

	/**
	 * Draws scene info, e.g. maze structure and special tiles
	 * 
	 * @param g graphics context
	 */
	protected void drawInfo(GraphicsContext g) {
		// empty by default
	}

	protected void drawLevelCounter(GraphicsContext g) {
		context.rendering2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	protected void drawMidwayCopyright(GraphicsContext g, int tileX, int tileY) {
		var r = context.rendering2D();
		drawText(g, "© 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, r.screenFont(TS), TS * tileX, TS * tileY);
	}
}