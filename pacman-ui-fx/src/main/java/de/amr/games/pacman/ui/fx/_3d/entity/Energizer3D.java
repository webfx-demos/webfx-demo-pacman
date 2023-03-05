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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * 3D energizer pellet.
 * 
 * @author Armin Reichert
 */
public class Energizer3D implements Eatable3D {

	private Shape3D shape;
	private Animation eatenAnimation;
	private final ScaleTransition pumping;

	public Energizer3D(Vector2i tile, PhongMaterial material) {

		shape = new Sphere(3.0);
		shape.setMaterial(material);

		shape.setTranslateX(tile.x() * TS + HTS);
		shape.setTranslateY(tile.y() * TS + HTS);
		shape.setTranslateZ(-HTS + 1);

		shape.setUserData(tile);

		pumping = new ScaleTransition(Duration.seconds(1.0 / 6), shape);
		pumping.setAutoReverse(true);
		pumping.setCycleCount(Animation.INDEFINITE);
		pumping.setFromX(1.0);
		pumping.setFromY(1.0);
		pumping.setFromZ(1.0);
		pumping.setToX(0.1);
		pumping.setToY(0.1);
		pumping.setToZ(0.1);
	}

	@Override
	public Node getRoot() {
		return shape;
	}

	public void init() {
		pumping.stop();
		shape.setScaleX(1.0);
		shape.setScaleY(1.0);
		shape.setScaleZ(1.0);
	}

	@Override
	public Optional<Animation> getEatenAnimation() {
		return Optional.ofNullable(eatenAnimation);
	}

	public void setEatenAnimation(Animation animation) {
		this.eatenAnimation = animation;
	}

	@Override
	public void eat() {
		pumping.stop();
		var hideAfterDelay = Ufx.afterSeconds(0.05, () -> shape.setVisible(false));
		if (eatenAnimation != null) {
			new SequentialTransition(hideAfterDelay, eatenAnimation).play();
		} else {
			hideAfterDelay.play();
		}
	}

	public void startPumping() {
		pumping.playFromStart();
	}

	public void stopPumping() {
		pumping.stop();
	}

	@Override
	public String toString() {
		return String.format("[Energizer, tile; %s, pumping: %s]", tile(), pumping.getStatus() == Status.RUNNING);
	}
}