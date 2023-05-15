package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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

    private final StackPane clickArea;

    public GreetingPane() {
        var ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.2f, 0.2f, 0.2f));

        var text = new Text("Play!");
        text.setEffect(ds);
        text.setCache(true);
        text.setFill(Color.WHITE);
        text.setFont(GameApp.assets.font(GameApp.assets.arcadeFont, 30));
        BorderPane.setAlignment(text, Pos.CENTER);

        // TODO that should probably be a button but GWT has its problems
        clickArea = new StackPane(text);
        clickArea.setMaxSize(200,100);
        clickArea.setPadding(new Insets(10));
        clickArea.setCursor(Cursor.HAND);
        clickArea.setBackground(ResourceManager.colorBackgroundRounded(
                ResourceManager.color(Color.rgb(0, 155, 252), 1.0), 20));

        setBottom(clickArea);
        BorderPane.setAlignment(clickArea, Pos.CENTER);
        clickArea.setTranslateY(-10);

        var bgImage = new BackgroundImage(
            GameApp.assets.greetingPaneWallpaper,
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
        clickArea.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                handler.run();
            }
        });
    }
}