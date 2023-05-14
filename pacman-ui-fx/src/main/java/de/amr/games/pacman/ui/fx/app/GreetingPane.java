package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
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

    private StackPane clickPane;

    public GreetingPane() {
        var ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.2f, 0.2f, 0.2f));

        var text = new Text("Click to start!");
        //text.setMouseTransparent(true);
        text.setEffect(ds);
        text.setCache(true);
        text.setFill(Color.WHITE);
        text.setFont(AppRes.Fonts.font(AppRes.Fonts.arcade, 24));
        BorderPane.setAlignment(text, Pos.CENTER);

        clickPane = new StackPane(text);
        clickPane.setMaxHeight(100);
        clickPane.setMaxWidth(400);
        clickPane.setPadding(new Insets(10));
        clickPane.setBackground(ResourceManager.colorBackground(
                ResourceManager.color(Color.CORNFLOWERBLUE, 0.8)));

        setBottom(clickPane);
        clickPane.setTranslateY(-20);
        BorderPane.setAlignment(clickPane, Pos.CENTER);

/*
        if (UserAgent.isBrowser()) {
            text.setTranslateY(20); //TODO fixme
        }
*/

        var bgImage = new BackgroundImage(
            AppRes.Graphics.greetingPaneWallpaper,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(AUTO, AUTO,false, false,
                true, false)
        );
        setBackground(new Background(bgImage));
    }

    /**
     * @param handler code executed when primary mouse button has been clicked inside the click pane
     */
    public void onClicked(Runnable handler) {
        clickPane.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                handler.run();
            }
        });
    }
}