package agenda.ui;
 
import agenda.dao.EvenementDAO;
import agenda.model.Evenement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
 
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
 
/**
 * Page dédiée aux événements de type "Rappel".
 * Accessible depuis le bouton Rappels de SplashView.
 */
public class Rappelview {
 
    private static final String RED      = "#e74c3c";
    private static final String DARKRED  = "#c0392b";
    private static final String WHITE    = "#ffffff";
    private static final String OFFWHITE = "#f9f9f9";
    private static final String LIGHTGRAY= "#f2f2f7";
    private static final String GRAY     = "#9b9b9b";
 
    private final EvenementDAO dao = new EvenementDAO();
    private final DateTimeFormatter fmt =
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
 
    public void start(Stage stage) {
        stage.setTitle("Rappels");
 
        // ---- En-tête ----
        Button btnBack = new Button("‹ Retour");
        btnBack.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: " + DARKRED + ";"
          + "-fx-font-size: 15; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> new SplashView().start(stage));
 
        Label titleLabel = new Label("Rappels");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(DARKRED));
 
        Label subtitleLabel = new Label("Vos événements à ne pas oublier");
        subtitleLabel.setFont(Font.font("System", 11));
        subtitleLabel.setTextFill(Color.web(GRAY));
 
        VBox titleBox = new VBox(2, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
 
        HBox header = new HBox(12, btnBack, titleBox);
        header.setPadding(new Insets(18, 20, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: " + WHITE + ";"
          + "-fx-border-color: #ebebeb; -fx-border-width: 0 0 1 0;");
 
        // ---- Barre de recherche ----
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un rappel...");
        searchField.setStyle(
            "-fx-background-color: " + LIGHTGRAY + ";"
          + "-fx-border-color: transparent; -fx-background-radius: 10;"
          + "-fx-padding: 8 12 8 12; -fx-font-size: 13;");
        HBox searchBar = new HBox(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.setPadding(new Insets(10, 16, 10, 16));
        searchBar.setStyle("-fx-background-color: " + WHITE + ";");
 
        // ---- Liste scrollable des rappels ----
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + OFFWHITE + "; -fx-background: " + OFFWHITE + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
 
        VBox listBox = new VBox(0);
        listBox.setStyle("-fx-background-color: " + OFFWHITE + ";");
        scroll.setContent(listBox);
 
        // ---- Statut / compteur ----
        Label countLabel = new Label();
        countLabel.setTextFill(Color.web(GRAY));
        countLabel.setFont(Font.font("System", 11));
        HBox statusBar = new HBox(countLabel);
        statusBar.setPadding(new Insets(6, 16, 8, 16));
        statusBar.setStyle(
            "-fx-background-color: " + OFFWHITE + ";"
          + "-fx-border-color: #ebebeb; -fx-border-width: 1 0 0 0;");
 
        // ---- Layout principal ----
        VBox root = new VBox(header, searchBar, scroll, statusBar);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color: " + OFFWHITE + ";");
 
        // ---- Chargement ----
        List<Evenement>[] allRappels = new List[1];
        try {
            allRappels[0] = dao.rechercher(null, null, "Rappel", false);
        } catch (SQLException ex) {
            allRappels[0] = List.of();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
        }
 
        afficherRappels(listBox, allRappels[0], countLabel);
 
        // ---- Recherche en temps réel ----
        searchField.textProperty().addListener((obs, old, nv) -> {
            String q = nv.trim().toLowerCase();
            List<Evenement> filtered = allRappels[0].stream()
                .filter(e -> e.getTitre().toLowerCase().contains(q)
                          || e.getDescription().toLowerCase().contains(q))
                .collect(Collectors.toList());
            afficherRappels(listBox, filtered, countLabel);
        });
 
        Scene scene = new Scene(root, 340, 720);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
 
    /** Construit les cartes de rappels dans la liste */
    private void afficherRappels(VBox container, List<Evenement> rappels, Label countLabel) {
        container.getChildren().clear();
 
        if (rappels.isEmpty()) {
            Label vide = new Label("Aucun rappel enregistré.");
            vide.setTextFill(Color.web(GRAY));
            vide.setFont(Font.font("System", 12));
            VBox empty = new VBox(vide);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            container.getChildren().add(empty);
            countLabel.setText("0 rappel");
            return;
        }
 
        // Séparer : à venir / passés
        LocalDate today = LocalDate.now();
        List<Evenement> aVenir = rappels.stream()
            .filter(e -> e.getDate() != null && !e.getDate().isBefore(today))
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toList());
 
        List<Evenement> passes = rappels.stream()
            .filter(e -> e.getDate() == null || e.getDate().isBefore(today))
            .sorted((a, b) -> {
                if (a.getDate() == null) return 1;
                if (b.getDate() == null) return -1;
                return b.getDate().compareTo(a.getDate()); // plus récent en tête
            })
            .collect(Collectors.toList());
 
        if (!aVenir.isEmpty()) {
            container.getChildren().add(sectionHeader("À venir", aVenir.size()));
            for (Evenement e : aVenir) container.getChildren().add(carteRappel(e, false));
        }
        if (!passes.isEmpty()) {
            container.getChildren().add(sectionHeader("Passés", passes.size()));
            for (Evenement e : passes) container.getChildren().add(carteRappel(e, true));
        }
 
        countLabel.setText(rappels.size() + " rappel" + (rappels.size() > 1 ? "s" : ""));
    }
 
    /** En-tête de section (À venir / Passés) */
    private HBox sectionHeader(String titre, int count) {
        Label lbl = new Label(titre.toUpperCase());
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(GRAY));
 
        Label cnt = new Label("(" + count + ")");
        cnt.setFont(Font.font("System", 11));
        cnt.setTextFill(Color.web(GRAY));
 
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
 
        HBox row = new HBox(6, lbl, cnt, spacer);
        row.setPadding(new Insets(16, 16, 6, 16));
        return row;
    }
 
    /** Carte d'un événement rappel */
    private HBox carteRappel(Evenement e, boolean passe) {
        // Pastille rouge / grise
        Label pastille = new Label("🔔");
        pastille.setFont(Font.font(15));
        pastille.setOpacity(passe ? 0.35 : 1.0);
        VBox pastilleBox = new VBox(pastille);
        pastilleBox.setAlignment(Pos.TOP_CENTER);
        pastilleBox.setPadding(new Insets(2, 12, 0, 0));
 
        // Contenu
        Label titre = new Label(e.getTitre());
        titre.setFont(Font.font("System", FontWeight.BOLD, 15));
        titre.setTextFill(Color.web(passe ? GRAY : "#1c1c1e"));
 
        String dateStr = (e.getDate() != null)
            ? e.getDate().format(fmt)
            : "Date non définie";
        String heureStr = (e.getHeure() != null && !e.getHeure().isBlank())
            ? "  ·  " + e.getHeure()
            : "";
 
        Label dateLbl = new Label(dateStr + heureStr);
        dateLbl.setFont(Font.font("System", 11));
        dateLbl.setTextFill(Color.web(passe ? GRAY : DARKRED));
 
        VBox textBox = new VBox(3, titre, dateLbl);
 
        if (e.getDescription() != null && !e.getDescription().isBlank()) {
            Label desc = new Label(e.getDescription());
            desc.setFont(Font.font("System", 11));
            desc.setTextFill(Color.web(GRAY));
            desc.setWrapText(true);
            textBox.getChildren().add(desc);
        }
 
        HBox card = new HBox(pastilleBox, textBox);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(
            "-fx-background-color: " + WHITE + ";"
          + "-fx-border-color: #f0f0f0;"
          + "-fx-border-width: 0 0 1 0;");
 
        // Survol discret
        card.setOnMouseEntered(ev -> card.setStyle(
            "-fx-background-color: #fff5f5;"
          + "-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;"));
        card.setOnMouseExited(ev -> card.setStyle(
            "-fx-background-color: " + WHITE + ";"
          + "-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;"));
 
        return card;
    }
}