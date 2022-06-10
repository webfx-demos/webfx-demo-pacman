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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * @author Armin Reichert
 */
public class GuysInfo {

	private static String bonusName(GameVariant gameVariant, int symbol) {
		return switch (gameVariant) {
		case MS_PACMAN -> MsPacManGame.BONUS_NAMES[symbol];
		case PACMAN -> PacManGame.BONUS_NAMES[symbol];
		};
	}

	private final PlayScene2D playScene;
	private GameModel game;
	private final Text[] texts = new Text[6];

	public GuysInfo(PlayScene2D playScene) {
		this.playScene = playScene;
		for (int i = 0; i < texts.length; ++i) {
			texts[i] = new Text();
			texts[i].setTextAlignment(TextAlignment.CENTER);
			texts[i].setFill(Color.WHITE);
		}
		playScene.infoPane.getChildren().addAll(texts);
	}

	public void init(GameModel game) {
		this.game = game;
	}

	private String getAnimationState(GenericAnimation<?> animation, Direction dir) {
		if (animation instanceof GenericAnimationMap) {
			@SuppressWarnings("unchecked")
			var gam = (GenericAnimationMap<Direction, ?>) animation;
			return gam.get(dir).isRunning() ? "" : "(Stopped) ";
		} else if (animation instanceof SingleGenericAnimation) {
			var ga = (SingleGenericAnimation<?>) animation;
			return ga.isRunning() ? "" : "(Stopped) ";
		} else {
			return "";
		}
	}

	private String computeGhostInfo(Ghost ghost) {
		String stateText = ghost.state.name();
		if (ghost.state == GhostState.HUNTING_PAC) {
			stateText += game.huntingTimer.chasingPhase() != -1 ? " (Chasing)" : " (Scattering)";
		}
		var animKey = ghost.animations().get().selectedKey();
		var animState = getAnimationState(ghost.animations().get().selectedAnimation(), ghost.wishDir());
		return "%s\n%s\n %s%s".formatted(ghost.name, stateText, animState, animKey);
	}

	private String computePacInfo(Pac pac) {
		if (pac.animations().isPresent()) {
			var animKey = pac.animations().get().selectedKey();
			var animState = getAnimationState(pac.animations().get().selectedAnimation(), pac.moveDir());
			return "%s\n%s%s".formatted(pac.name, animState, animKey);
		} else {
			return "%s\n".formatted(pac.name);
		}
	}

	private String computeBonusInfo(Bonus bonus) {
		var symbolName = bonus.state() == BonusState.INACTIVE ? "INACTIVE" : bonusName(game.variant, bonus.symbol());
		if (bonus.animations().isPresent()) {
			return "%s\n%s\n%s".formatted(symbolName, game.bonus().state(), bonus.animations().get().selectedKey());
		} else {
			return "%s\n%s".formatted(symbolName, game.bonus().state());
		}
	}

	private void updateTextView(Text textView, String text, Entity entity) {
		textView.setText(text);
		var textSize = textView.getBoundsInLocal();
		var scaling = playScene.currentScaling();
		textView.setX((entity.position.x + World.HTS) * scaling - textSize.getWidth() / 2);
		textView.setY(entity.position.y * scaling - textSize.getHeight());
		textView.setVisible(entity.visible);
	}

	private void updateTextView(Text textView, String text, Bonus bonus) {
		textView.setText(text);
		var textSize = textView.getBoundsInLocal();
		var scaling = playScene.currentScaling();
		textView.setX((bonus.entity().position.x + World.HTS) * scaling - textSize.getWidth() / 2);
		textView.setY(bonus.entity().position.y * scaling - textSize.getHeight());
		textView.setVisible(bonus.state() != BonusState.INACTIVE);
	}

	public void update() {
		for (int i = 0; i < texts.length; ++i) {
			if (i < texts.length - 2) {
				var ghost = game.ghosts[i];
				updateTextView(texts[i], computeGhostInfo(ghost), ghost);
			} else if (i == texts.length - 2) {
				updateTextView(texts[i], computePacInfo(game.pac), game.pac);
			} else {
				updateTextView(texts[i], computeBonusInfo(game.bonus()), game.bonus());
			}
		}
	}
}