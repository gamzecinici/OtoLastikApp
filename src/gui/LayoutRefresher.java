package gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Ekran yenileme (layout refresh) yardımcı sınıfı.
 * Sadece 1 px küçültüp geri büyütür (ters yönlü hile).
 */
public class LayoutRefresher {

    public static void refresh(Node anyNode) {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) anyNode.getScene().getWindow();
                double w = stage.getWidth();
                double h = stage.getHeight();

                // 🔹 Eğer tam ekran değilse 1 px azalt → sonra geri artır
                if (!stage.isMaximized()) {
                    stage.setWidth(w - 1);
                    stage.setHeight(h - 1);

                    PauseTransition pause = new PauseTransition(Duration.millis(100));
                    pause.setOnFinished(e -> {
                        stage.setWidth(w);
                        stage.setHeight(h);
                    });
                    pause.play();
                } else {
                    // 🔹 Tam ekranda sadece layout’u tazele
                    anyNode.getScene().getRoot().requestLayout();
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Layout refresh hatası: " + ex.getMessage());
            }
        });
    }
}
