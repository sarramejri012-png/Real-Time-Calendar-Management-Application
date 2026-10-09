package agenda.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.File;

public class SplashView {

    // === Dimensions exactes de mon screenshot ===
    private static final double IMG_W = 340;
    private static final double IMG_H = 720;

    public void start(Stage stage) {
        stage.setTitle("Agenda");

        String cheminImage = "C:\\Users\\DELL\\Desktop\\SEMESTRE2\\POO2\\projet calander\\src\\agenda\\resources\\phone_screen.jpeg";
        Image img = new Image(new File(cheminImage).toURI().toString());

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(IMG_W);
        imageView.setFitHeight(IMG_H);
        imageView.setPreserveRatio(false); // forcer exactement la taille

        // Bouton Calendrier (icone calendrier sur l'écran)
        Button btnCalendrier = new Button("");
        btnCalendrier.setLayoutX(113);
        btnCalendrier.setLayoutY(265);
        btnCalendrier.setPrefWidth(61);
        btnCalendrier.setPrefHeight(61);
        btnCalendrier.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnCalendrier.setOnAction(e -> new MainView().start(stage));

        // Bouton Rappels → ouvre la page des événements Rappel
        Button btnSecret = new Button("");
        btnSecret.setLayoutX(192);
        btnSecret.setLayoutY(341);
        btnSecret.setPrefWidth(61);
        btnSecret.setPrefHeight(61);
        btnSecret.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnSecret.setOnAction(e -> new Rappelview().start(stage)); // ← page dédiée

        Pane root = new Pane();
        root.getChildren().addAll(imageView, btnCalendrier, btnSecret);

        Scene scene = new Scene(root, IMG_W, IMG_H);
        stage.setScene(scene);
        stage.setResizable(false); // taille fixe comme un téléphone
        stage.show();
    }
}