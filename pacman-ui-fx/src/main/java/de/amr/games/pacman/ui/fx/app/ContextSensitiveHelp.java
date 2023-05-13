/*
MIT License

Copyright (c) 2023 Armin Reichert

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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class ContextSensitiveHelp {

	private static class Menu {
		private final List<Node> column0 = new ArrayList<>();
		private final List<Node> column1 = new ArrayList<>();

		public void addRow(Node node0, Node node1) {
			column0.add(node0);
			column1.add(node1);
		}

		public int size() {
			return column0.size();
		}
	}

	private final Map<String, String> translations;
	private final GameController gameController;
	private final FadeTransition closeAnimation;
	private Font font = Font.font("Helvetica", 10);

	public ContextSensitiveHelp(GameController gameController, Map<String, String> translations) {
		this.gameController = gameController;
		this.translations = translations;
		closeAnimation = new FadeTransition(Duration.seconds(0.5));
		closeAnimation.setFromValue(1);
		closeAnimation.setToValue(0);
	}

	public void show(GameScene2D scene2d, Duration openDuration) {
		if (closeAnimation.getStatus() == Animation.Status.RUNNING) {
			closeAnimation.stop();
		}
		var pane = currentPane();
		if (pane == null) {
			scene2d.helpRoot().getChildren().clear();
		} else {
			scene2d.helpRoot().getChildren().setAll(pane);
		}
		scene2d.helpRoot().setOpacity(1);
		closeAnimation.setNode(scene2d.helpRoot());
		closeAnimation.setDelay(openDuration);
		closeAnimation.play();
	}

	public void clear(GameScene2D scene2d) {
		closeAnimation.stop();
		scene2d.helpRoot().getChildren().clear();
	}

	private Pane currentPane() {
		Pane pane = null;
		switch (gameController.state()) {
			case CREDIT:
				pane = menuCredit();
				break;
			case INTRO:
				pane = menuIntro();
				break;
			case READY: case HUNTING: case PACMAN_DYING: case GHOST_DYING:
				pane = attractMode() ? menuDemoLevel() : menuPlaying();
				break;
			default:
				break;
		}
		return pane;
	}
	private Pane createPane(Menu menu) {
		var grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		for (int row = 0; row < menu.column0.size(); ++row) {
			grid.add(menu.column0.get(row), 0, row);
			grid.add(menu.column1.get(row), 1, row);
		}
		int rowIndex = menu.size();
		if (gameController.isAutoControlled()) {
			var text = text(tt("help.autopilot_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, rowIndex++);
		}
		if (gameController.game().isImmune()) {
			var text = text(tt("help.immunity_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, rowIndex++);
		}

		var pane = new BorderPane(grid);
		pane.setPadding(new Insets(10));
		switch (gameController.game().variant()) {
			case MS_PACMAN:
				pane.setBackground(ResourceManager.colorBackground(Color.rgb(255, 0, 0, 0.9)));
				break;
			case PACMAN:
				pane.setBackground(ResourceManager.colorBackground(Color.rgb(33, 33, 255, 0.9)));
				break;
			default: throw new IllegalGameVariantException(gameController.game().variant());
		}

		//TODO workaround for GWT layout issues
/*
		if (UserAgent.isBrowser()) {
			grid.setTranslateY(6);
		}
*/

		return pane;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	private String tt(String key) {
		return translations.get(key);
	}

	private Node label(String s) {
		// TODO In JavaFX 11, I get ellipsis for label texts and don't know how to avoid that
/*		var label = new Label(s);
		label.setTextFill(Color.gray(0.9));
		label.setFont(font);
		return label;*/
		return text(s, Color.gray(0.9));
	}

	private Text text(String s, Color color) {
		var text = new Text(s);
		text.setFill(color);
		text.setFont(font);
		return text;
	}

	private Text text(String s) {
		return text(s, Color.YELLOW);
	}

	private GameModel game() {
		return gameController.game();
	}

	private boolean attractMode() {
		var gameLevel = game().level();
		return gameLevel.isPresent() && gameLevel.get().isDemoLevel();
	}

	private void addEntry(Menu menu, String rbKey, String kbKey) {
		menu.addRow(label(tt(rbKey)), text("[" + kbKey + "]"));
	}

	private Pane menuIntro() {
		var menu = new Menu();
		if (game().credit() > 0) {
			addEntry(menu, "help.start_game", "1");
		}
		addEntry(menu, "help.add_credit", "5");
		addEntry(menu, "help.show_help", "H");
		addEntry(menu, game().variant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman", "V");
		return createPane(menu);
	}

	private Pane menuCredit() {
		var menu = new Menu();
		if (game().credit() > 0) {
			addEntry(menu, "help.start_game", "1");
		}
		addEntry(menu, "help.add_credit", "5");
		addEntry(menu, "help.show_help", "H");
		addEntry(menu, "help.show_intro", "Q");
		return createPane(menu);
	}

	private Pane menuPlaying() {
		var menu = new Menu();
		addEntry(menu, "help.move_left", tt("help.cursor_left"));
		addEntry(menu, "help.move_right", tt("help.cursor_right"));
		addEntry(menu, "help.move_up", tt("help.cursor_up"));
		addEntry(menu, "help.move_down", tt("help.cursor_down"));
		addEntry(menu, "help.show_help", "H");
		addEntry(menu, "help.show_intro", "Q");
		return createPane(menu);
	}

	private Pane menuDemoLevel() {
		var menu = new Menu();
		addEntry(menu, "help.add_credit", "5");
		addEntry(menu, "help.show_help", "H");
		addEntry(menu, "help.show_intro", "Q");
		return createPane(menu);
	}
}