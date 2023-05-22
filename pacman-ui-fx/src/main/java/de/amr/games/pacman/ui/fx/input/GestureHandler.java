/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.ui.fx.input;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.scene.Node;
import org.tinylog.Logger;

import java.util.function.Consumer;

/**
 * @author Armin Reichert, Bruno Salmon
 */
public class GestureHandler {

	private final GameSceneContext context;
	private Consumer<Direction> dirConsumer = dir -> Logger.info("Move {}", dir);
	private boolean alternateIntermediateDirection;

	public GestureHandler(Node node, GameSceneContext context) {
		this.context = context;
		node.setOnMouseDragged(event -> {
			var dir = computeDirection(event.getX(), event.getY());
			if (dir != null)
				dirConsumer.accept(dir);
		});
	}

	private Direction computeDirection(double gestureX, double gestureY) {
		GameLevel level = context.game().level().orElse(null);
		if (level == null)
			return null;
		var pos = level.pac().center();
		if (Math.abs(gestureX - pos.x()) < 8 && Math.abs(gestureY - pos.y()) < 8) {
			return null;
		}
		// Angle between Pac-Man and the mouse (between -180° and +180°)
		double angle = Math.atan2(pos.y() - gestureY, gestureX - pos.x()) / Math.PI * 180;
		// We will consider all cardinal directions (RIGHT, UP, LEFT & DOWN) and all intermediate directions (RIGHT_UP,
		// LEFT_UP, LEFT_DOWN & RIGHT_DOWN) => 8 directions => 360 / 8 = 45° each. RIGHT direction starts at
		// angleStart = -45/2 = -22.5° up to angleStart + 45°, and so on (+45° each time). To make things easier, we
		// shift the angle by adding 22.5° (so RIGHT will finally be between 0 & 45°, RIGHT_UP between 45° & 90°, etc...)
		double shiftedAngle = (angle + 45d / 2 + 360d) % 360d; // Also we ensure shiftedAngle is between 0° and 360°
		// Because intermediate directions don't exist in Direction enum, we simulate them by alternating between the 2
		// closest cardinal directions. Ex: to simulate RIGHT_UP, we return first RIGHT, next time UP, then RIGHT, UP...
		alternateIntermediateDirection = !alternateIntermediateDirection;
		if (shiftedAngle <= 45)   return Direction.RIGHT;
		if (shiftedAngle <= 90)   return alternateIntermediateDirection ? Direction.RIGHT : Direction.UP; // RIGHT_UP intermediate direction
		if (shiftedAngle <= 135 ) return Direction.UP;
		if (shiftedAngle <= 180)  return alternateIntermediateDirection ? Direction.LEFT : Direction.UP; // LEFT_UP intermediate direction
		if (shiftedAngle <= 225)  return Direction.LEFT;
		if (shiftedAngle <= 270)  return alternateIntermediateDirection ? Direction.LEFT : Direction.DOWN; // LEFT_DOWN intermediate direction
		if (shiftedAngle <= 315)  return Direction.DOWN;
		return alternateIntermediateDirection ? Direction.RIGHT : Direction.DOWN; // RIGHT_DOWN intermediate direction
	}

	public void setOnDirectionRecognized(Consumer<Direction> dirConsumer) {
		this.dirConsumer = dirConsumer;
	}

}