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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Armin Reichert
 */
public class ContextSensitiveHelp {

	private static class Row {
		String left;
		String right;

		public Row(String left, String right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Row row = (Row) o;
			return Objects.equals(left, row.left) && Objects.equals(right, row.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, right);
		}
	}

	public static class Help {

		private final List<Row> table = new ArrayList<>();

		public void addRow(String left, String right) {
			table.add(new Row(left, right));
		}

		private Text text(String s, Font font, Color color) {
			var text = new Text(s);
			text.setFill(color);
			text.setFont(font);
			return text;
		}

		public Pane createPane(GameController gameController, Font font) {
			var grid = new GridPane();
			grid.setHgap(30);
			grid.setVgap(10);
			var leftColor = AppRes.ArcadeTheme.PALE;
			var rightColor = AppRes.ArcadeTheme.YELLOW;
			for (int rowIndex = 0; rowIndex < table.size(); ++rowIndex) {
				var row = table.get(rowIndex);
				var leftCol = text(row.left, font, leftColor);
				GridPane.setHalignment(leftCol, HPos.RIGHT);
				grid.add(leftCol, 0, rowIndex);
				var rightCol = text(row.right, font, rightColor);
				GridPane.setHalignment(rightCol, HPos.LEFT);
				grid.add(rightCol, 1, rowIndex);
			}
			int rowIndex = table.size();
			if (gameController.isAutoControlled()) {
				var col = text("(AUTOPILOT ON)", font, AppRes.ArcadeTheme.ORANGE);
				GridPane.setColumnSpan(col, 2);
				GridPane.setHalignment(col, HPos.CENTER);
				grid.add(col, 0, rowIndex);
				++rowIndex;
			}
			if (gameController.game().isImmune()) {
				var col = text("(IMMUNITY ON)", font, AppRes.ArcadeTheme.ORANGE);
				GridPane.setColumnSpan(col, 2);
				grid.add(col, 0, rowIndex);
				GridPane.setHalignment(col, HPos.CENTER);
				++rowIndex;
			}

			var pane = new BorderPane();
			pane.setLeft(grid);
			//pane.setMaxSize(100, 250);
			pane.setPadding(new Insets(25));
			//pane.setBackground(AppRes.Manager.colorBackground(Color.rgb(200, 200, 200, 0.35)));
			return pane;
		}
	}

	private final GameController gameController;

	public ContextSensitiveHelp(GameController gameController) {
		this.gameController = gameController;
	}

	private GameModel game() {
		return gameController.game();
	}

	private GameVariant variant() {
		return game().variant();
	}

	private boolean attractMode() {
		return game().level().isPresent() && game().level().get().isDemoLevel();
	}

	public Optional<Help> current() {
		Help help = null;
		switch (gameController.state()) {
			case CREDIT:
				help = helpCredit();
			break;
			case INTRO:
				help =  helpIntro();
			break;
			case READY:
			case HUNTING:
			case PACMAN_DYING:
			case GHOST_DYING:
				help = attractMode() ? helpDemoLevel() : helpPlaying();
				break;
			default:
				break;
		}
		return Optional.ofNullable(help);
	}

	private Help helpIntro() {
		var help = new Help();
		if (game().credit() > 0) {
			help.addRow("START GAME", "1");
		}
		help.addRow("ADD CREDIT", "5");
		help.addRow(variant() == GameVariant.MS_PACMAN ? "PAC-MAN" : "MS.PAC-MAN", "V");
		return help;
	}

	private Help helpCredit() {
		var help = new Help();
		if (game().credit() > 0) {
			help.addRow("START GAME", "1");
		}
		help.addRow("ADD CREDIT", "5");
		help.addRow("QUIT", "Q");
		return help;
	}

	private Help helpPlaying() {
		var help = new Help();
		help.addRow("MOVE LEFT", "CURSOR LEFT");
		help.addRow("MOVE RIGHT", "CURSOR RIGHT");
		help.addRow("MOVE UP", "CURSOR UP");
		help.addRow("MOVE DOWN", "CURSOR DOWN");
		help.addRow("QUIT", "Q");
		return help;
	}

	private Help helpDemoLevel() {
		var help = new Help();
		help.addRow("ADD CREDIT", "5");
		help.addRow("QUIT", "Q");
		return help;
	}
}