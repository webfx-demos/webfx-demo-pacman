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
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
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
import javafx.scene.transform.Scale;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;

/**
 * Base class of all 2D scenes. Each 2D scene has its own canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameEventListener {

	private static final float WIDTH = World.TILES_X * TS;
	private static final float HEIGHT = World.TILES_Y * TS;
	private static final float ASPECT_RATIO = WIDTH / HEIGHT;
	private static final Color UNDERLAY_COLOR = Color.rgb(248, 249, 249);

	protected final GameSceneContext context;
	protected final BorderPane root = new BorderPane();
	protected final StackPane layers = new StackPane();
	protected final Pane underlay = new BorderPane();
	protected final Canvas canvas = new Canvas();
	protected final Pane overlay = new Pane();

	protected final VBox helpPanelContainer = new VBox();
	protected ImageView helpButton;

	protected GameScene2D(GameController gameController) {
		checkNotNull(gameController);
		context = new GameSceneContext(gameController);

		root.setScaleX(0.99);
		root.setScaleY(0.99);

		root.heightProperty().addListener((py, ov, nv) -> {
			double scaling = nv.doubleValue() / HEIGHT;
			canvas.setScaleX(0.95 * scaling);
			canvas.setScaleY(0.95 * scaling);
			// don't ask me why this works but setScaleX/Y doesn't
			overlay.getTransforms().setAll(new Scale(scaling,scaling));
		});

		underlay.setBackground(ResourceManager.colorBackgroundRounded(UNDERLAY_COLOR, 10));

		canvas.setWidth(WIDTH);
		canvas.setHeight(HEIGHT);

		helpPanelContainer.setTranslateX(15);
		helpPanelContainer.setTranslateY(18);

		overlay.setMouseTransparent(true);
		overlay.getChildren().add(helpPanelContainer);

		layers.getChildren().addAll(underlay, canvas, overlay);
		root.setCenter(layers);


		insertHelpButton(GameVariant.PACMAN);
	}

	// TODO: Graphic button rendering is broken in GWT
	private void insertHelpButton(GameVariant variant) {
		helpButton = new ImageView(GameApp.assets.helpIcon);
		helpButton.setOnMouseClicked(e -> GameApp.app.showHelp());
		helpButton.setPreserveRatio(true);
		helpButton.setFitHeight(32);
		helpButton.setFitWidth(32);
		helpButton.setCursor(Cursor.HAND);
		helpButton.setTranslateX(-34);
		helpButton.setTranslateY(2);
		underlay.getChildren().add(helpButton);
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

	public void render() {
		var g = canvas.getGraphicsContext2D();
		drawRoundedCanvasBackground(g);
		if (context.isScoreVisible()) {
			var r = context.rendering2D();
			var color = ArcadeTheme.PALE;
			var font = r.screenFont(TS);
			context.game().score().ifPresent(score -> r.drawScore(g, score, "SCORE", font, color, TS, TS));
			context.game().highScore().ifPresent(score -> r.drawScore(g, score, "HIGH SCORE", font, color, TS * 13, TS));
			if (context.isCreditVisible()) {
				Rendering2D.drawText(g, "CREDIT " + context.game().credit(), color, font,TS * 2, TS * 36 - 1);
			}
		}
		drawScene(g);
	}

	protected void drawRoundedCanvasBackground(GraphicsContext g) {
		double w = canvas.getWidth();
		double h = canvas.getHeight();

		g.setFill(Color.WHITE);
		g.fillRect(0, 0, w, h);

		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, w, h,10,8);
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 * 
	 * @param g graphics context
	 */
	protected abstract void drawScene(GraphicsContext g);

	protected void drawLevelCounter(GraphicsContext g) {
		context.rendering2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	protected void drawMidwayCopyright(GraphicsContext g, int tileX, int tileY) {
		drawText(g, "© 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, GameApp.assets.arcadeFont, TS * tileX, TS * tileY);
	}
}