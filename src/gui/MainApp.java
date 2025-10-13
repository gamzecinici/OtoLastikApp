package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/gui/login.fxml"));
        primaryStage.setTitle("Yılmaz & Ünal Oto Lastik - Giriş");
        primaryStage.setScene(new Scene(root, 800, 450)); // FXML boyutlarıyla aynı
        primaryStage.setResizable(false); // Boyut sabit kalsın
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
