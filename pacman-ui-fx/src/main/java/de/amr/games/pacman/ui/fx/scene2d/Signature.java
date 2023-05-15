package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.GameApp;
import de.amr.games.pacman.ui.fx.app.GameAssets;
import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Signature {

    private final Text part1;
    private final Text part2;

    public Signature() {
        part1 = new Text("Remake (2023) by ");
        part1.setFill(Color.gray(0.42));
        part1.setFont(Font.font("Helvetica", 9));

        part2 = new Text("Armin Reichert");
        part2.setFill(Color.gray(0.42));
        part2.setFont(GameAssets.font(GameApp.assets.handwritingFont, 9));
    }

    public void add(Pane parent, double x, double y) {
        part1.setTranslateX(x);
        part1.setTranslateY(y);
        part2.setTranslateX(x+80);
        part2.setTranslateY(y);
        parent.getChildren().addAll(part1, part2);
    }

    public void show() {
        List<Transition> partTransitions = new ArrayList<>();
        Stream.of(part1, part2).forEach(part-> {
            var fadeIn = new FadeTransition(Duration.seconds(5), part);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(Interpolator.EASE_IN);

            var fadeOut = new FadeTransition(Duration.seconds(1), part);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            partTransitions.add(new SequentialTransition(fadeIn, fadeOut));
        });
        var animation = new ParallelTransition();
        animation.getChildren().addAll(partTransitions);
        animation.play();
    }

    public void setOpacity(double value) {
        part1.setOpacity(value);
        part2.setOpacity(value);
    }
}