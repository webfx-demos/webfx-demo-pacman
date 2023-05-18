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

package de.amr.games.pacman.ui.fx.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 *
 * TODO This whole thing needs to be rethought.
 */
public class Keyboard {

	private static KeyEvent currentEvent;

	/**
	 * If the event is not yet consumed, it is stored and can be matched against key combinations.
	 * 
	 * @param e key event
	 */
	public static void accept(KeyEvent e) {
		if (e.isConsumed()) {
			currentEvent = null;
			Logger.trace("Ignored key event ({}): {}", e.getCode(), e);
		} else {
			currentEvent = e;
			e.consume();
			Logger.trace("Consumed key event ({}): {}", e.getCode(), e);
		}
	}

	public static boolean pressed(KeyCodeCombination combination) {
		if (currentEvent == null) {
			return false;
		}
		// French keyboard with AZERTY layout delivers KeyCode.Q when pressing "A" key! (Les Français sont marrant :-)
		if (currentEvent.getCode().isLetterKey()) {
			var letter = currentEvent.getText().toUpperCase();
			var letterCode = KeyCode.getKeyCode(letter);
			if (!letterCode.equals(currentEvent.getCode())) {
				Logger.info("Replace code " + letterCode + " in event " + currentEvent);
				currentEvent = replaceKeyCode(currentEvent, letterCode);
				Logger.info("Modified event: " + currentEvent);
			}
		}
		return combination.match(currentEvent);
	}

	private static KeyEvent replaceKeyCode(KeyEvent event, KeyCode newCode) {
		return new KeyEvent(event.getSource(), event.getTarget(), event.getEventType(), event.getCharacter(),
				event.getText(), event.getCode(), event.isShiftDown(), event.isControlDown(), event.isAltDown(),
				event.isMetaDown());
	}

	public static void clearState() {
		currentEvent = null;
	}

	private Keyboard() {
	}
}