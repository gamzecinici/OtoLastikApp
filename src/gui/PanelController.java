package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PanelController {

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private ImageView logoView;

    @FXML
    public void initialize() {
        // Logo yükle (resources/images/logo.png yolunda olmalı)
        //logoView.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));

        // Saat ve tarih güncelle
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            dateLabel.setText(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void handleLastikler() {
        System.out.println("➡ Lastikler sayfasına geçiş");
        // Burada yeni FXML yükleyebilirsin
    }

    @FXML
    private void handleLastikEkle() {
        System.out.println("➡ Lastik ekleme sayfasına geçiş");
    }

    @FXML
    private void handleSatislar() {
        System.out.println("➡ Veresiyeler sayfasına geçiş");
    }
}