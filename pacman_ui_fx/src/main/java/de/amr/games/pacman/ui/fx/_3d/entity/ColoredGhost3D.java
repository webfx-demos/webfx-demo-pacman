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
package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

	private final int ghostID;
	private final Model3D model3D;
	private final Group root3D;

	public final ObjectProperty<Color> dressColorPy;
	private final ObjectProperty<Color> eyeBallsColorPy;
	private final ObjectProperty<Color> eyePupilsColorPy;

	private ParallelTransition flashing;
	private ColorFlashing dressFlashing;
	private ColorFlashing pupilsFlashing;

	public ColoredGhost3D(int ghostID, Model3D model3D) {
		this.ghostID = ghostID;
		this.model3D = model3D;

		var dressColor = Rendering3D.getGhostDressColor(ghostID);
		var eyeBallColor = Rendering3D.getGhostEyeBallColor();
		var pupilColor = Rendering3D.getGhostPupilColorBlue();

		root3D = model3D.createGhost(dressColor, eyeBallColor, pupilColor);

		dressColorPy = new SimpleObjectProperty<>(dressColor);
		var dressMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(dressMaterial, dressColorPy);
		dress().setMaterial(dressMaterial);

		eyeBallsColorPy = new SimpleObjectProperty<>(Rendering3D.getGhostEyeBallColor());
		var eyeBallsMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyeBallsMaterial, eyeBallsColorPy);
		model3D.ghostEyeBalls(root3D).setMaterial(eyeBallsMaterial);

		eyePupilsColorPy = new SimpleObjectProperty<>(Rendering3D.getGhostPupilColorBlue());
		var eyePupilsMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyePupilsMaterial, eyePupilsColorPy);
		model3D.ghostEyePupils(root3D).setMaterial(eyePupilsMaterial);
	}

	private void createFlashing(int numFlashes) {
		var seconds = GameModel.PAC_POWER_FADING_TICKS / (2 * 60.0); // 2 animation cycles = 1 flashing
		dressFlashing = new ColorFlashing(Rendering3D.getGhostDressColorBlue(), Rendering3D.getGhostDressColorFlashing(),
				seconds, numFlashes);
		pupilsFlashing = new ColorFlashing(Rendering3D.getGhostPupilColorPink(), Rendering3D.getGhostPupilColorRed(),
				seconds, numFlashes);
		flashing = new ParallelTransition(dressFlashing, pupilsFlashing);
	}

	public Shape3D dress() {
		return model3D.ghostDress(root3D);
	}

	public Node getRoot() {
		return root3D;
	}

	private void ensureFlashingPlaying(int numFlashes) {
		if (flashing == null) {
			createFlashing(numFlashes);
		}
		if (flashing.getStatus() != Status.RUNNING) {
			flashing.playFromStart();
		}
	}

	private void ensureFlashingStopped() {
		if (flashing != null && flashing.getStatus() == Status.RUNNING) {
			flashing.stop();
			flashing = null;
		}
	}

	public void wearBlueDress(int numFlashes) {
		if (numFlashes > 0) {
			ensureFlashingPlaying(numFlashes);
			dressColorPy.bind(dressFlashing.colorPy);
			eyePupilsColorPy.bind(pupilsFlashing.colorPy);
			eyeBallsColorPy.set(Rendering3D.getGhostEyeBallColorFrightened());
		} else {
			ensureFlashingStopped();
			dressColorPy.unbind();
			eyePupilsColorPy.unbind();
			dressColorPy.set(Rendering3D.getGhostDressColorBlue());
			eyePupilsColorPy.set(Rendering3D.getGhostPupilColorRed());
			eyeBallsColorPy.set(Rendering3D.getGhostEyeBallColorFrightened());
		}
		dress().setVisible(true);
	}

	public void wearColoredDress() {
		dressColorPy.unbind();
		eyePupilsColorPy.unbind();
		ensureFlashingStopped();
		dressColorPy.set(Rendering3D.getGhostDressColor(ghostID));
		eyeBallsColorPy.set(Rendering3D.getGhostEyeBallColor());
		eyePupilsColorPy.set(Rendering3D.getGhostPupilColorBlue());
		dress().setVisible(true);
	}
}