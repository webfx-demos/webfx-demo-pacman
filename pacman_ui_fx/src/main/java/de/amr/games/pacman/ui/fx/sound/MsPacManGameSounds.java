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

package de.amr.games.pacman.ui.fx.sound;

import java.util.Map;

import de.amr.games.pacman.model.common.GameSound;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 *
 */
public class MsPacManGameSounds extends AbstractGameSounds {

	private void put(Map<GameSound, AudioClip> map, GameSound sound, String path) {
		super.put(map, sound, getClass().getResource(path));
	}

	public MsPacManGameSounds() {
		//@formatter:off
		put(clips, GameSound.BONUS_EATEN,     "mspacman/Fruit.mp3");
		put(clips, GameSound.CREDIT,          "mspacman/Coin Credit.mp3");
		put(clips, GameSound.EXTRA_LIFE,      "mspacman/Extra Life.mp3");
		put(clips, GameSound.GAME_READY,      "mspacman/Start.mp3");
		put(clips, GameSound.GHOST_EATEN,     "mspacman/Ghost.mp3");
		put(clips, GameSound.GHOST_RETURNING, "mspacman/Ghost Eyes.mp3");
		put(clips, GameSound.INTERMISSION_1,  "mspacman/They Meet Act 1.mp3");
		put(clips, GameSound.INTERMISSION_2,  "mspacman/The Chase Act 2.mp3");
		put(clips, GameSound.INTERMISSION_3,  "mspacman/Junior Act 3.mp3");
		put(clips, GameSound.PACMAN_MUNCH,    "mspacman/Ms. Pac Man Pill.mp3");
		put(clips, GameSound.PACMAN_DEATH,    "mspacman/Died.mp3");
		put(clips, GameSound.PACMAN_POWER,    "mspacman/Scared Ghost.mp3");
		put(clips, GameSound.SIREN_1,         "mspacman/Ghost Noise 1.mp3");
		put(clips, GameSound.SIREN_2,         "mspacman/Ghost Noise 2.mp3");
		put(clips, GameSound.SIREN_3,         "mspacman/Ghost Noise 3.mp3");
		put(clips, GameSound.SIREN_4,         "mspacman/Ghost Noise 4.mp3");
		//@formatter:on
	}
}
