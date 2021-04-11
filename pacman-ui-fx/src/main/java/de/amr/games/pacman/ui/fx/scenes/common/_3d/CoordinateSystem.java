package de.amr.games.pacman.ui.fx.scenes.common._3d;

import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * TODO: fixme
 * 
 * @author Armin Reichert
 */
public class CoordinateSystem {

	private final Group root;

	public CoordinateSystem(double axisLength) {
		Sphere origin = new Sphere(1);
		origin.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		Cylinder xAxis = createAxis(Color.RED.brighter(), axisLength);
		Cylinder yAxis = createAxis(Color.GREEN.brighter(), axisLength);
		Cylinder zAxis = createAxis(Color.BLUE.brighter(), axisLength / 2);

		xAxis.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
		zAxis.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

		root = new Group(origin, xAxis, yAxis, zAxis);
		root.visibleProperty().bind(Env.$axesVisible);
	}

	// Cylinder height points to y-direction
	private Cylinder createAxis(Color color, double height) {
		Cylinder axis = new Cylinder(0.25, height);
		axis.setMaterial(new PhongMaterial(color));
		return axis;
	}

	public Node getNode() {
		return root;
	}
}