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
package de.amr.games.pacman.ui.fx._3d.scene;

import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.CoordSystem;
import javafx.beans.binding.DoubleExpression;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public abstract class GameScene3D implements GameScene {

	protected final SubScene fxSubScene;
	protected final Group contentRoot = new Group();
	protected AmbientLight light;
	protected CoordSystem coordSystem;
	protected SceneContext ctx;

	protected GameScene3D(double width, double height) {
		coordSystem = new CoordSystem(1000);
		coordSystem.visibleProperty().bind(Env.axesVisiblePy);
		light = new AmbientLight(Env.lightColorPy.get());
		light.colorProperty().bind(Env.lightColorPy);
		// origin is at center of scene content
		contentRoot.getTransforms().add(new Translate(-width / 2, -height / 2));
		// initial size does not matter, subscene is resized automatically
		fxSubScene = new SubScene(new Group(contentRoot, coordSystem, light), 50, 50, true, SceneAntialiasing.BALANCED);
	}

	protected GameScene3D() {
		this(ArcadeWorld.TILES_X * World.TS, ArcadeWorld.TILES_Y * World.TS);
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void setSceneContext(SceneContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	@Override
	public void setResizeBehavior(DoubleExpression width, DoubleExpression height) {
		fxSubScene.widthProperty().bind(width);
		fxSubScene.heightProperty().bind(height);
	}

	public GameSceneCamera getCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	public void setCamera(GameSceneCamera camera) {
		fxSubScene.setCamera(camera);
		fxSubScene.setOnKeyPressed(camera::onKeyPressed);
		fxSubScene.requestFocus();
	}

	protected void blockGameController() {
		ctx.state().timer().resetIndefinitely();
	}

	protected void unblockGameController() {
		ctx.state().timer().expire();
	}
}