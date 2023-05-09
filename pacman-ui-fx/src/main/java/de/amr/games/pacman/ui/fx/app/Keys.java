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

package de.amr.games.pacman.ui.fx.app;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * @author Armin Reichert
 */
public class Keys {

	private static KeyCodeCombination just(KeyCode code) {
		return new KeyCodeCombination(code);
	}

	private static KeyCodeCombination alt(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
	}

	private static KeyCodeCombination shift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
	}

	private static KeyCodeCombination altShift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN);
	}

	public static final KeyCodeCombination HELP = just(KeyCode.H);
	public static final KeyCodeCombination AUTOPILOT = altShift(KeyCode.A);
	public static final KeyCodeCombination BOOT = just(KeyCode.Z); // TODO does not work with any combination?
	public static final KeyCodeCombination CHEAT_EAT_ALL = altShift(KeyCode.E);
	public static final KeyCodeCombination IMMUNITY = altShift(KeyCode.I);
	public static final KeyCodeCombination CHEAT_ADD_LIVES = altShift(KeyCode.L);
	public static final KeyCodeCombination CHEAT_NEXT_LEVEL = altShift(KeyCode.N);
	public static final KeyCodeCombination CHEAT_KILL_GHOSTS = altShift(KeyCode.X);
	public static final KeyCodeCombination TEST_LEVELS = altShift(KeyCode.T);
	public static final KeyCodeCombination PLAY_CUTSCENES = altShift(KeyCode.C);

	public static final KeyCodeCombination PAUSE = just(KeyCode.P);
	public static final KeyCodeCombination PAUSE_STEP = shift(KeyCode.P);
	public static final KeyCodeCombination SINGLE_STEP = just(KeyCode.SPACE);
	public static final KeyCodeCombination TEN_STEPS = shift(KeyCode.SPACE);
	public static final KeyCodeCombination SIMULATION_FASTER = alt(KeyCode.F);
	public static final KeyCodeCombination SIMULATION_SLOWER = alt(KeyCode.G);
	public static final KeyCodeCombination SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

	public static final KeyCodeCombination QUIT = just(KeyCode.Q);
	public static final KeyCodeCombination SELECT_VARIANT = just(KeyCode.V);

	public static final KeyCodeCombination START_GAME = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination ADD_CREDIT = just(KeyCode.DIGIT5);
}