package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class MainController {

    @FXML private AnchorPane contentArea;

    @FXML
    public void initialize() {
        // Sahne yüklendikten hemen sonra bu kod çalışır:
        javafx.application.Platform.runLater(() -> {
            contentArea.getScene().setUserData(this);
        });

        // İlk sayfa yükleme
        loadView("/gui/Panel.fxml");
        LayoutRefresher.refresh(contentArea);
    }

    /**
     * 🔹 FXML sayfalarını contentArea içerisine yükler.
     */
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            contentArea.getChildren().clear();
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
