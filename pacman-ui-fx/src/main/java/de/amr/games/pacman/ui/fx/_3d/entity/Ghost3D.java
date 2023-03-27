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

import java.util.Objects;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import de.amr.games.pacman.ui.fx._3d.animation.MoveAnimation;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D representation of a ghost.
 * <p>
 * A ghost is displayed in one of the following modes:
 * <ul>
 * <li>normal: colored ghost with blue eyes,
 * <li>frightened: blue ghost with empty pinkish eyes (ghost looking blind),
 * <li>frightened/flashing: blue-white flashing skin, pink-red flashing eyes,
 * <li>dead: blue eyes only,
 * <li>eaten: number cube showing eaten ghost's value.
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D {

	private enum Look {
		NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER;
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final Ghost ghost;
	private final MoveAnimation moveAnimation;
	private RotateTransition brakeEffect;
	private final Group root = new Group();
	private final ColoredGhost3D coloredGhost3D;
	private Image numberImage;
	private Look look;

	public Ghost3D(Ghost ghost, GhostColoring colors) {
		this.ghost = Objects.requireNonNull(ghost, "Ghost must not be null");
		Objects.requireNonNull(colors, "Ghost colors must not be null");
		coloredGhost3D = new ColoredGhost3D(colors);
		coloredGhost3D.dress().drawModeProperty().bind(drawModePy);
		coloredGhost3D.eyeBalls().drawModeProperty().bind(drawModePy);
		coloredGhost3D.pupils().drawModeProperty().bind(drawModePy);
		moveAnimation = new MoveAnimation(root, ghost);
	}

	public Node getRoot() {
		return root;
	}

	public void init(GameLevel level) {
		moveAnimation.init();
		update(level);
	}

	public void update(GameLevel level) {
		var newLook = computeLookForCurrentState(level);
		if (look != newLook) {
			changeLook(level, newLook);
		}
		if (look != Look.NUMBER) {
			moveAnimation.update();
		}
		// brake effect
		if (level.world().isTunnel(ghost.tile())) {
			brakeEffect = new RotateTransition(Duration.seconds(0.2), coloredGhost3D.getRoot());
			brakeEffect.setAxis(Rotate.Y_AXIS);
			brakeEffect.setToAngle(-25);
			brakeEffect.play();
		} else {
			if (brakeEffect != null) {
				brakeEffect.stop();
				coloredGhost3D.getRoot().setRotationAxis(Rotate.Y_AXIS);
				coloredGhost3D.getRoot().setRotate(0);
			}
		}
		root.setVisible(ghost.isVisible() && !outsideWorld(level)); // ???
	}

	public void setNumberImage(Image numberImage) {
		this.numberImage = numberImage;
	}

	private void changeLook(GameLevel level, Look newLook) {
		look = newLook;
		switch (newLook) {
		case NORMAL -> {
			coloredGhost3D.appearNormal();
			moveAnimation.init();
			root.getChildren().setAll(coloredGhost3D.getRoot());
		}
		case FRIGHTENED -> {
			coloredGhost3D.appearFrightened();
			root.getChildren().setAll(coloredGhost3D.getRoot());
		}
		case FLASHING -> {
			int numFlashes = level.numFlashes;
			if (numFlashes > 0) {
				coloredGhost3D.appearFlashing(numFlashes, 1.0);
			} else {
				coloredGhost3D.appearFrightened();
			}
			root.getChildren().setAll(coloredGhost3D.getRoot());
		}
		case EYES -> {
			coloredGhost3D.appearEyesOnly();
			moveAnimation.init();
			root.getChildren().setAll(coloredGhost3D.getRoot());
		}
		case NUMBER -> {
			var material = new PhongMaterial();
			material.setBumpMap(numberImage);
			material.setDiffuseMap(numberImage);
			var numberCube = new Box(World.TS, World.TS, World.TS);
			numberCube.setMaterial(material);
			root.getChildren().setAll(numberCube);
			// rotate node such that number can be read from left to right
			root.setRotationAxis(Rotate.X_AXIS);
			root.setRotate(0);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + newLook);
		}
	}

	private boolean outsideWorld(GameLevel level) {
		double centerX = ghost.position().x() + World.HTS;
		return centerX < 0 || centerX > level.world().numCols() * World.TS;
	}

	private Look computeLookForCurrentState(GameLevel level) {
		return switch (ghost.state()) {
		case LOCKED, LEAVING_HOUSE -> normalOrFrightenedOrFlashingLook(level);
		case FRIGHTENED -> frightenedOrFlashingLook(level);
		case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
		case EATEN -> Look.NUMBER;
		default -> Look.NORMAL;
		};
	}

	private Look normalOrFrightenedOrFlashingLook(GameLevel level) {
		if (level.pac().powerTimer().isRunning() && ghost.killedIndex() == -1) {
			return frightenedOrFlashingLook(level);
		}
		return Look.NORMAL;
	}

	private Look frightenedOrFlashingLook(GameLevel level) {
		return level.pac().isPowerFading(level) ? Look.FLASHING : Look.FRIGHTENED;
	}
}