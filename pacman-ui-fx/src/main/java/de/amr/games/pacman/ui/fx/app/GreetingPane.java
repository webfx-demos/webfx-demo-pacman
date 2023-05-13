package de.amr.games.pacman.ui.fx.app;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * This is shown at application start (in the browser) to guarantee that a user interaction happens.
 * Before the first user interaction, no sound can be played.
 *
 * @author Armin Reichert
 */
public class GreetingPane extends BorderPane {

    public GreetingPane() {
        var ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.2f, 0.2f, 0.2f));

        var text = new Text("Click to start!");
        text.setMouseTransparent(true);
        text.setEffect(ds);
        text.setCache(true);
        text.setFill(Color.YELLOW);
        text.setFont(AppRes.Fonts.font(AppRes.Fonts.arcade, 20));
        BorderPane.setAlignment(text, Pos.CENTER);
        text.setTranslateY(20); //TODO
        setCenter(text);

        var bgImage = new BackgroundImage(
            AppRes.Graphics.msPacManCabinet,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(AUTO, AUTO,false, false,
                false, true)
        );
        setBackground(new Background(bgImage));
    }
}
