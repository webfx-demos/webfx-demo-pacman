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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Picture-In-Picture view. Displays an embedded 2D game scene.
 * 
 * @author Armin Reichert
 */
public class PiPView {

	public static final double MIN_WIDTH = 28 * 8;
	public static final double MIN_HEIGHT = 36 * 8;

	public final DoubleProperty heightPy = new SimpleDoubleProperty() {
		@Override
		protected void invalidated() {
			thumbnail.resize(get());
		}
	};

	private final StackPane root = new StackPane();
	private final GameScene2D thumbnail = new PlayScene2D();

	public PiPView() {
		root.setBackground(Ufx.colorBackground(Color.BLACK));
		root.setFocusTraversable(false);
		root.getChildren().add(thumbnail.getFXSubScene());
		thumbnail.resize(MIN_HEIGHT);
		thumbnail.getOverlayCanvas().visibleProperty().unbind();
	}

	public StackPane getRoot() {
		return root;
	}

	public void draw(GameScene gameScene) {
		var g = thumbnail.getGameSceneCanvas().getGraphicsContext2D();
		thumbnail.setSceneContext(gameScene.getSceneContext());
		thumbnail.renderScene(g);
		thumbnail.drawHUD(g);
	}
}