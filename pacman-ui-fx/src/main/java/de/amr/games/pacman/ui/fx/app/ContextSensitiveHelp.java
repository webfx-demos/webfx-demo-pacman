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
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.Arrays;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Armin Reichert
 */
public class ContextSensitiveHelp {

	private final Map<String, String> translations;
	private final GameController gameController;
	private Color backgroundColor = Color.WHITE;
	private Font font = Font.font("Helvetica", 8);

	public ContextSensitiveHelp(GameController gameController, Map<String, String> translations) {
		this.gameController = gameController;
		this.translations = translations;
	}

	public Optional<Pane> current() {
		Pane pane = null;
		switch (gameController.state()) {
			case CREDIT:
				pane = helpCredit();
				break;
			case INTRO:
				pane = helpIntro();
				break;
			case READY: case HUNTING: case PACMAN_DYING: case GHOST_DYING:
				pane = attractMode() ? helpDemoLevel() : helpPlaying();
				break;
			default:
				break;
		}
		return Optional.ofNullable(pane);
	}

	public void setGameVariant(GameVariant variant) {
		switch (variant) {
			case MS_PACMAN:
				setBackgroundColor(Color.rgb(255, 0, 0, 0.9));
				break;
			case PACMAN:
				setBackgroundColor(Color.rgb(33, 33, 255, 0.9));
				break;
			default:
				throw new IllegalGameVariantException(variant);
		}
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	private String tt(String key) {
		return translations.get(key);
	}

	private Text label(String s) {
		var label = new Text(s);
		label.setFill(Color.gray(0.9));
		label.setFont(font);
		return label;
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

	private Text key(String key) {
		return text("[" + key + "]");
	}

	private class Help {

		private final List<List<Node>> rows = new ArrayList<>();

		public void addRow(String labelText, String keySpec) {
			rows.add(Arrays.asList(label(labelText), key(keySpec)));
		}

		public Pane createPane() {
			var grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			for (int rowIndex = 0; rowIndex < rows.size(); ++rowIndex) {
				var row = rows.get(rowIndex);
				grid.add(row.get(0), 0, rowIndex);
				grid.add(row.get(1), 1, rowIndex);
			}
			if (gameController.isAutoControlled()) {
				var text = text(tt("help.autopilot_on"), Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rows.size());
			}
			if (gameController.game().isImmune()) {
				var text = text(tt("help.immunity_on"), Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rows.size() + 1);
			}

			var pane = new BorderPane(grid);
			pane.setPadding(new Insets(10));
			pane.setBackground(ResourceManager.colorBackground(backgroundColor));

			//TODO this is a workaround for GWT layout issues
			if (UserAgent.isBrowser()) {
				grid.setTranslateY(6);
			}
			return pane;
		}
	}

	private GameModel game() {
		return gameController.game();
	}

	private boolean attractMode() {
		var gameLevel = game().level();
		return gameLevel.isPresent() && gameLevel.get().isDemoLevel();
	}

	private Pane helpIntro() {
		var help = new Help();
		if (game().credit() > 0) {
			help.addRow(tt("help.start_game"), "1");
		}
		help.addRow(tt("help.add_credit"), "5");
		help.addRow(tt(game().variant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman"), "V");
		return help.createPane();
	}

	private Pane helpCredit() {
		var help = new Help();
		if (game().credit() > 0) {
			help.addRow(tt("help.start_game"), "1");
		}
		help.addRow(tt("help.add_credit"), "5");
		help.addRow(tt("help.show_intro"), "Q");
		return help.createPane();
	}

	private Help helpPlaying;

	private Pane helpPlaying() {
		if (helpPlaying == null) {
			var help = new Help();
			help.addRow(tt("help.move_left"), tt("help.cursor_left"));
			help.addRow(tt("help.move_right"), tt("help.cursor_right"));
			help.addRow(tt("help.move_up"), tt("help.cursor_up"));
			help.addRow(tt("help.move_down"), tt("help.cursor_down"));
			help.addRow(tt("help.show_intro"), "Q");
			helpPlaying = help;
		}
		return helpPlaying.createPane();
	}

	private Help helpDemoLevel;

	private Pane helpDemoLevel() {
		if (helpDemoLevel == null) {
			var help = new Help();
			help.addRow(tt("help.start_game"), "5");
			help.addRow(tt("help.show_intro"), "Q");
			helpDemoLevel = help;
		}
		return helpDemoLevel.createPane();
	}
}