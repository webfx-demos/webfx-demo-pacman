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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.ObjModel;
import de.amr.games.pacman.ui.fx._3d.animation.MoveAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.PacDyingAnimation;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	private static final ObjModel OBJ_MODEL = new ObjModel("model3D/pacman.obj");
	private static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	private static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	private static final String MESH_ID_PALATE = "Sphere_grey_wall";

	private static final Color HEAD_COLOR = Color.YELLOW;
	private static final Color EYES_COLOR = Color.rgb(33, 33, 33);
	private static final Color PALATE_COLOR = Color.rgb(191, 79, 61);

	private static final double PAC_SIZE = 9.0;

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	/**
	 * @param eyesColor   Pac-Man eyes color
	 * @param palateColor Pac-Man palate color
	 * @return transformation group representing a 3D Pac-Man.
	 */
	public static Group createTG(Color headColor, Color eyesColor, Color palateColor) {
		var head = new MeshView(OBJ_MODEL.mesh(MESH_ID_HEAD));
		head.setMaterial(new PhongMaterial(headColor));

		var eyes = new MeshView(OBJ_MODEL.mesh(MESH_ID_EYES));
		eyes.setMaterial(new PhongMaterial(eyesColor));

		var palate = new MeshView(OBJ_MODEL.mesh(MESH_ID_PALATE));
		palate.setMaterial(new PhongMaterial(palateColor));

		var centerTransform = centerOverOrigin(head);
		Stream.of(head, eyes, palate).forEach(meshView -> meshView.getTransforms().add(centerTransform));

		var group = new Group(head, eyes, palate);
		group.getTransforms().addAll(new Translate(0, 0, -1), scale(group, PAC_SIZE), new Rotate(90, Rotate.X_AXIS));

		return group;
	}

	public static Shape3D head(Group root) {
		return (Shape3D) root.getChildren().get(0);
	}

	public static Shape3D eyes(Group root) {
		return (Shape3D) root.getChildren().get(1);
	}

	public static Shape3D palate(Group root) {
		return (Shape3D) root.getChildren().get(2);
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", HEAD_COLOR);

	private final Pac pac;
	private final MoveAnimation moveAnimation;
	private final Group root;

	public Pac3D(Pac pac) {
		this.pac = pac;
		root = createTG(HEAD_COLOR, EYES_COLOR, PALATE_COLOR);
		Stream.of(head(root), eyes(root), palate(root)).forEach(part -> part.drawModeProperty().bind(drawModePy));
		moveAnimation = new MoveAnimation(root, pac);
	}

	public Group getRoot() {
		return root;
	}

	public void init() {
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		moveAnimation.init();
		moveAnimation.update();
		headColorPy.set(HEAD_COLOR);
	}

	public void update(GameLevel level) {
		moveAnimation.update();
		if (outsideWorld(level.world())) {
			root.setVisible(false);
		} else {
			root.setVisible(pac.isVisible());
		}
	}

	private boolean outsideWorld(World world) {
		double centerX = pac.position().x() + HTS;
		return centerX < HTS || centerX > world.numCols() * TS - HTS;
	}

	/**
	 * @return dying animation (must not be longer than time reserved by game controller which is 5 seconds!)
	 */
	public Animation createDyingAnimation() {
		return new PacDyingAnimation(root).getAnimation();
	}
}