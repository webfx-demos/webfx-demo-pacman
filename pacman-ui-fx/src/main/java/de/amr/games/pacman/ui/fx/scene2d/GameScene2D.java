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
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.AppRes.ArcadeTheme;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.animation.*;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

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
	protected ImageView helpIcon;

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

		canvas.setWidth(WIDTH);
		canvas.setHeight(HEIGHT);

		// help panel appears in overlay layer at left border, 10% from top
		overlay.getChildren().add(helpRoot);
		helpRoot.setTranslateX(8);
		helpRoot.setTranslateY(HEIGHT * 0.1);

		helpIcon = new ImageView(AppRes.Graphics.iconHelp);
		helpIcon.setPreserveRatio(true);
		helpIcon.setFitHeight(24);
		helpIcon.setTranslateX(-28);
		helpIcon.setTranslateY(helpRoot.getTranslateY()-24);
		helpIcon.setOnMouseEntered(e -> Actions.toggleHelp());
		overlay.getChildren().add(helpIcon);

		layers.getChildren().addAll(canvas, overlay);
		root.setCenter(layers);

		infoVisiblePy.bind(Env.showDebugInfoPy);
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
		g.setFill(Color.GRAY);
		g.fillRect(0, 0, w, h);
		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, w, h, 20, 20);

		var color = ArcadeTheme.PALE;
		var font = r.screenFont(TS);
		if (context.isScoreVisible()) {
			context.game().score().ifPresent(score -> r.drawScore(g, score, "SCORE", font, color, TS, TS));
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
		drawText(g, "© 1980 MIDWAY MFG.CO.", AppRes.ArcadeTheme.PINK, r.screenFont(TS), TS * tileX, TS * tileY);
	}

	protected List<Text> addSignature(double x, double y) {
		var t1 = new Text("Remake (2023) by ");
		t1.setFill(Color.gray(0.75));
		t1.setFont(Font.font("Helvetica", 9));
		t1.setTranslateX(x);
		t1.setTranslateY(y);

		var t2 = new Text("Armin Reichert");
		t2.setFill(Color.gray(0.75));
		t2.setFont(AppRes.Fonts.font(AppRes.Fonts.handwriting, 9));
		t2.setTranslateX(x+80);
		t2.setTranslateY(y);

		var signature = new ArrayList<Text>();
		signature.add(t1);
		signature.add(t2);

		overlay.getChildren().addAll(t1,t2);
		return signature;
	}

	protected void showSignature(List<Text> signature) {
		List<Transition> partTransitions = new ArrayList<>();
		for (Text part : signature) {
			var fadeIn = new FadeTransition(Duration.seconds(5), part);
			fadeIn.setFromValue(0);
			fadeIn.setToValue(1);
			fadeIn.setInterpolator(Interpolator.EASE_IN);

			var fadeOut = new FadeTransition(Duration.seconds(1), part);
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0);

			partTransitions.add(new SequentialTransition(fadeIn, fadeOut));
		}
		var animation = new ParallelTransition();
		for (Transition part : partTransitions) {
			animation.getChildren().add(part);
		}
		animation.play();
	}
}