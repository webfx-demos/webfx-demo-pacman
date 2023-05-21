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
 * @author Armin Reichert
 */
public class GestureHandler {

	private final GameSceneContext context;
	private Consumer<Direction> dirConsumer = dir -> Logger.info("Move {}", dir);

	public GestureHandler(Node node, GameSceneContext context) {
		this.context = context;
		// This works in the desktop implementation but not with GWT:
		//node.setOnDragDetected(event -> {

		// When dragging the mouse and releasing it, a series of drag events is generated followed
		// by a mouse release event.
		// So the pattern (drag+ release) forms a "swipe" gesture and the mouse position
		// between the *first* drag and the release event carries the information for computing the direction.
		// TODO: Does this also work on a touchscreen?
		node.setOnMouseDragged(event -> {
			var dir = computeDirection(event.getX(), event.getY());
			if (dir != null)
				dirConsumer.accept(dir);
		});
		/*node.setOnMouseReleased(event -> {
			Logger.info("Released " + event);
			if (dragged) {
				gestureEndX = event.getX();
				gestureEndY = event.getY();
				var dir = computeDirection();
				Logger.info("Move " + dir);
				dirConsumer.accept(dir);
				dragged = false;
			}
		});*/
	}

	private Direction computeDirection(double gestureX, double gestureY) {
		GameLevel level = context.game().level().orElse(null);
		if (level == null)
			return null;
		var pos = level.pac().center();
		double dx = gestureX - pos.x();
		double dy = gestureY - pos.y();
		if (Math.abs(dx) > Math.abs(dy)) {
			// horizontal
			return dx > 0 ? Direction.RIGHT : Direction.LEFT;
		} else {
			// vertical
			return dy > 0 ? Direction.DOWN : Direction.UP;
		}
	}

	public void setOnDirectionRecognized(Consumer<Direction> dirConsumer) {
		this.dirConsumer = dirConsumer;
	}

}