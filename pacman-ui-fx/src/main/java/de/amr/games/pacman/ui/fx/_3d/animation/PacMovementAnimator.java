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
package de.amr.games.pacman.ui.fx._3d.animation;

import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._3d.entity.Turn;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class PacMovementAnimator {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Duration NODDING_DURATION = Duration.seconds(0.2);

	public final BooleanProperty noddingPy = new SimpleBooleanProperty(this, "nodding", false) {
		@Override
		protected void invalidated() {
			if (get()) {
				createNoddingAnimation();
			} else {
				endNoddingAnimation();
				nodding = null;
			}
		}
	};

	private final Pac pac;
	private final Node pacNode;
	private final Rotate moveDirRotate;
	private RotateTransition nodding;

	public PacMovementAnimator(Pac pac, Node pacNode) {
		this.pac = Objects.requireNonNull(pac);
		this.pacNode = Objects.requireNonNull(pacNode);
		moveDirRotate = new Rotate(moveDirAngle(), Rotate.Z_AXIS);
		pacNode.getTransforms().setAll(moveDirRotate);
	}

	private void createNoddingAnimation() {
		nodding = new RotateTransition(NODDING_DURATION, pacNode);
		nodding.setFromAngle(-30);
		nodding.setToAngle(30);
		nodding.setCycleCount(Animation.INDEFINITE);
		nodding.setAutoReverse(true);
		nodding.setInterpolator(Interpolator.EASE_BOTH);
	}

	public void init() {
		endNoddingAnimation();
		update();
	}

	public void update() {
		pacNode.setTranslateX(pac.center().x());
		pacNode.setTranslateY(pac.center().y());
		pacNode.setTranslateZ(-HTS);
		moveDirRotate.setAngle(moveDirAngle());
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
		pacNode.setRotationAxis(axis);
		if (nodding != null) {
			updateNodding();
		}
	}

	private double moveDirAngle() {
		return Turn.angle(pac.moveDir());
	}

	private void updateNodding() {
		if (pac.velocity().length() == 0 || !pac.moveResult.moved || pac.restingTicks() == Pac.REST_FOREVER) {
			endNoddingAnimation();
			pacNode.setRotate(0);
		} else if (nodding.getStatus() != Status.RUNNING) {
			var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			nodding.setAxis(axis);
			nodding.playFromStart();
			LOG.trace("%s: Nodding created and started", pac.name());
		}
	}

	private void endNoddingAnimation() {
		if (nodding != null && nodding.getStatus() == Status.RUNNING) {
			nodding.stop();
			LOG.trace("%s: Nodding stopped", pac.name());
		}
	}
}