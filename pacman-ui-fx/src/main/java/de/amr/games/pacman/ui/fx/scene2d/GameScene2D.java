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
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.fx.app.GameApp;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;

/**
 * Base class of all 2D scenes. Each 2D scene has its own canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameEventListener {

	public static final float WIDTH  = 28 * 8;
	public static final float HEIGHT = 36 * 8;
	public static final float ASPECT_RATIO = WIDTH / HEIGHT;

	private static final Color IPAD_FRAME_COLOR = Color.rgb(240, 240, 240);

	protected final BorderPane root = new BorderPane();
	protected final StackPane layers = new StackPane();
	protected final Pane underlay = new BorderPane();
	protected final Canvas behindCanvas = new Canvas();
	protected final Canvas canvas = new Canvas();
	protected final Pane overlay = new Pane();
	protected final VBox helpPanelContainer = new VBox();
	protected ImageView helpButton;

	protected final GameSceneContext context;

	protected GameScene2D(GameController gameController) {
		checkNotNull(gameController);
		context = new GameSceneContext(gameController);

		root.setScaleX(0.98);
		root.setScaleY(0.98);

		underlay.setBackground(ResourceManager.colorBackgroundRounded(IPAD_FRAME_COLOR, 10));

		behindCanvas.setWidth(WIDTH);
		behindCanvas.setHeight(HEIGHT);

		canvas.setWidth(WIDTH);
		canvas.setHeight(HEIGHT);

		// position where left-top corner of help popup appears
		helpPanelContainer.setTranslateX(16);
		helpPanelContainer.setTranslateY(HEIGHT * 0.25);
		overlay.getChildren().add(helpPanelContainer);

		layers.getChildren().addAll(underlay, behindCanvas, canvas, overlay);
		root.setCenter(layers);

		insertHelpButton(8, WIDTH /2 - 4, HEIGHT - 9);

		// for iPad look, we need a cam of course
		var cam = new Circle(0.8, Color.gray(0.25));
		cam.setTranslateX(WIDTH/2);
		cam.setTranslateY(6);
		overlay.getChildren().add(cam);

		root.heightProperty().addListener((py, ov, nv) -> {
			double magnification = (nv.doubleValue() / HEIGHT); // ratio between screen and model height
			double canvasShrink = 0.90;
			double canvasScaling = magnification * canvasShrink;
			canvas.setScaleX(canvasScaling);
			canvas.setScaleY(canvasScaling);
			behindCanvas.setScaleX(canvasScaling * 1.05);
			behindCanvas.setScaleY(canvasScaling * 1.025);
			overlay.getTransforms().setAll(new Scale(magnification, magnification));
		});
	}

	public void render() {
		var g = canvas.getGraphicsContext2D();
		var r = context.rendering2D();
		drawRoundedCanvasBackground(IPAD_FRAME_COLOR, Color.BLACK);
		if (context.isScoreVisible()) {
			context.game().score().ifPresent(score ->
					r.drawScore(g, score, "SCORE", GameApp.assets.arcadeFont, ArcadeTheme.PALE, TS, TS));
			context.game().highScore().ifPresent(score ->
					r.drawScore(g, score, "HIGH SCORE", GameApp.assets.arcadeFont, ArcadeTheme.PALE, TS * 13, TS));
		}
		if (context.isCreditVisible()) {
			Rendering2D.drawText(g, "CREDIT " + context.game().credit(), ArcadeTheme.PALE, GameApp.assets.arcadeFont,TS * 2, TS * 36 - 1);
		}
		drawScene(g);
		drawLevelCounter(g);
	}

	protected void drawRoundedCanvasBackground(Color frameColor, Color canvasColor) {
		// small black padding around the game canvas
		var bgCtx = behindCanvas.getGraphicsContext2D();
		bgCtx.setFill(Color.BLACK);
		// That doesn't look good:
		//bgCtx.fillRoundRect(0, 0, behindCanvas.getWidth(), behindCanvas.getHeight(), 10, 10);
		bgCtx.fillRect(0, 0, behindCanvas.getWidth(), behindCanvas.getHeight());

		var g = canvas.getGraphicsContext2D();
		g.setFill(canvasColor);
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	// TODO: Graphic button rendering is broken in GWT, so I use an image with mouse handlers
	private void insertHelpButton(double size, double x, double y) {
		helpButton = new ImageView(GameApp.assets.helpIcon);
		helpButton.setOnMouseEntered(e -> helpButton.setImage(GameApp.assets.helpIconHover));
		helpButton.setOnMouseExited(e -> helpButton.setImage(GameApp.assets.helpIcon));
		helpButton.setOnMouseClicked(e -> {
			Logger.info("Help button mouse clicked: " + e);
			e.consume();
			GameApp.app.showHelp();
		});
		helpButton.setPreserveRatio(true);
		helpButton.setFitHeight(size);
		helpButton.setFitWidth(size);
		helpButton.setCursor(Cursor.HAND);
		helpButton.setTranslateX(x);
		helpButton.setTranslateY(y);
		overlay.getChildren().add(helpButton);
	}

	public void init() {
		// empty default
	}

	public void update() {
		// empty default
	}

	public void end() {
		// empty default
	}

	public void handleKeyboardInput() {
		// empty default
	}

	public GameSceneContext context() {
		return context;
	}

	public Node root() {
		return root;
	}

	public VBox helpRoot() {
		return helpPanelContainer;
	}

	public ImageView helpButton() {
		return helpButton;
	}

	public void onEmbedIntoParent(Pane parent) {
		resize(parent.getHeight());
	}

	public void onParentResize(Pane parent) {
		resize(parent.getHeight());
	}

	public void resize(double height) {
		if (height <= 0) {
			throw new IllegalArgumentException("Scene height must be positive");
		}
		var width = ASPECT_RATIO * height;
		root.setMinSize(width, height);
		root.setMaxSize(width, height);
	}

	protected abstract void drawScene(GraphicsContext g);

	private void drawLevelCounter(GraphicsContext g) {
		context.rendering2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	protected void drawMidwayCopyright(GraphicsContext g, int tileX, int tileY) {
		drawText(g, "© 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, GameApp.assets.arcadeFont, TS * tileX, TS * tileY);
	}
}