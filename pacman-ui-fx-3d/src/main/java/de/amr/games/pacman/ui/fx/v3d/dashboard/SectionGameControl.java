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
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.v3d.app.GameUI3d;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class SectionGameControl extends Section {

	private static final int GAME_LEVEL_START = 0;
	private static final int GAME_LEVEL_QUIT = 1;
	private static final int GAME_LEVEL_NEXT = 2;

	private static final int INTERMISSION_TEST_START = 0;
	private static final int INTERMISSION_TEST_QUIT = 1;

	private ComboBox<GameVariant> comboGameVariant;
	private ComboBox<Integer> comboInitialLives;
	private Button[] blGameLevel;
	private Button[] blIntermissionTest;
	private Spinner<Integer> spGameLevel;
	private Spinner<Integer> spGameCredit;
	private CheckBox cbAutopilot;
	private CheckBox cbImmunity;

	public SectionGameControl(GameUI3d ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		comboGameVariant = addComboBox("Variant", GameVariant.MS_PACMAN, GameVariant.PACMAN);
		comboGameVariant.setOnAction(e -> {
			if (comboGameVariant.getValue() != game().variant()) {
				gc.selectGameVariant(comboGameVariant.getValue());
			}
		});

		comboInitialLives = addComboBox("Initial Lives", 3, 5);
		comboInitialLives.setOnAction(e -> GameModel.initialLives = comboInitialLives.getValue());

		blGameLevel = addButtonList("Game Level", "Start", "Quit", "Next");
		blGameLevel[GAME_LEVEL_START].setOnAction(e -> gc.startPlaying());
		blGameLevel[GAME_LEVEL_QUIT].setOnAction(e -> Actions.restartIntro());
		blGameLevel[GAME_LEVEL_NEXT].setOnAction(e -> gc.cheatEnterNextLevel());

		blIntermissionTest = addButtonList("Cut Scenes Test", "Start", "Quit");
		blIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> Actions.startCutscenesTest());
		blIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> Actions.restartIntro());

		spGameLevel = addSpinner("Level", 1, 100, 1);
		spGameLevel.valueProperty().addListener((obs, oldVal, newVal) -> Actions.enterLevel(newVal.intValue()));

		spGameCredit = addSpinner("Credit", 0, GameModel.MAX_CREDIT, game().credit());
		spGameCredit.valueProperty().addListener((obs, oldVal, newVal) -> game().setCredit(newVal.intValue()));

		cbAutopilot = addCheckBox("Autopilot", Actions::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", Actions::toggleImmunity);
	}

	@Override
	public void update() {
		super.update();

		comboGameVariant.setValue(game().variant());
		comboGameVariant.setDisable(gc.state() != GameState.INTRO);

		comboInitialLives.setValue(GameModel.initialLives);

		cbAutopilot.setSelected(gc.isAutoControlled());
		cbImmunity.setSelected(game().isImmune());

		blGameLevel[GAME_LEVEL_START].setDisable(!(game().hasCredit() && game().level().isEmpty()));

		blGameLevel[GAME_LEVEL_QUIT].setDisable(game().level().isEmpty());

		blGameLevel[GAME_LEVEL_NEXT].setDisable(//
				!game().isPlaying() //
						|| (gc.state() != GameState.HUNTING && gc.state() != GameState.READY
								&& gc.state() != GameState.CHANGING_TO_NEXT_LEVEL));

		blIntermissionTest[INTERMISSION_TEST_START].setDisable(//
				gc.state() == GameState.INTERMISSION_TEST || gc.state() != GameState.INTRO);

		blIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(gc.state() != GameState.INTERMISSION_TEST);

		game().level().ifPresent(level -> spGameLevel.getValueFactory().setValue(level.number()));
		if (!game().isPlaying() || gc.state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			spGameLevel.setDisable(true);
		} else {
			spGameLevel.setDisable(gc.state() != GameState.READY && gc.state() != GameState.HUNTING
					&& gc.state() != GameState.CHANGING_TO_NEXT_LEVEL);
		}

		spGameCredit.getValueFactory().setValue(game().credit());
	}
}