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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * 
 * @author Armin Reichert
 *
 * @param <K> key type of map (enum)
 */
public class SpriteAnimationMap<K extends Enum<K>> implements ISpriteAnimation {

	private final Map<K, SpriteAnimation> animationMap;

	public SpriteAnimationMap(Class<K> keyClass) {
		animationMap = new EnumMap<>(keyClass);
	}

	public void put(K key, SpriteAnimation animation) {
		animationMap.put(key, animation);
	}

	public SpriteAnimation get(K key) {
		return animationMap.get(key);
	}

	public Collection<SpriteAnimation> values() {
		return animationMap.values();
	}

	@Override
	public void reset() {
		values().forEach(SpriteAnimation::reset);
	}

	public void restart() {
		values().forEach(SpriteAnimation::restart);
	}

	public void stop() {
		values().forEach(SpriteAnimation::stop);
	}

	public void run() {
		values().forEach(SpriteAnimation::run);
	}

	@Override
	public void ensureRunning() {
		values().forEach(animation -> {
			if (!animation.isRunning()) {
				animation.run();
			}
		});
	}
}