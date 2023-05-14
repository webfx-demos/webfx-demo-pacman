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

package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.app.ArcadeTheme;
import de.amr.games.pacman.ui.fx.app.GameActions;
import de.amr.games.pacman.ui.fx.app.GameApp;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.canvas.GraphicsContext;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	public PacManCreditScene(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		// Nothing to do
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(GameActions.ADD_CREDIT)) {
			GameApp.actions.addCredit();
		} else if (Keyboard.pressed(GameActions.START_GAME)) {
			GameApp.actions.startGame();
		}
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var r = context.rendering2D();
		var normalFont = r.screenFont(TS);
		var smallFont = r.screenFont(6); // TODO looks ugly
		drawText(g, "PUSH START BUTTON", ArcadeTheme.ORANGE, normalFont, TS * (6), TS * (17));
		drawText(g, "1 PLAYER ONLY", ArcadeTheme.CYAN, normalFont, TS * (8), TS * (21));
		drawText(g, "BONUS PAC-MAN FOR 10000", ArcadeTheme.ROSE, normalFont, TS * (1), TS * (25));
		drawText(g, "PTS", ArcadeTheme.ROSE, smallFont, TS * (25), TS * (25));
		drawMidwayCopyright(g, 4, 29);
		drawLevelCounter(g);
	}
}