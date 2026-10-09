package agenda.ui;
 
import agenda.dao.EvenementDAO;
import agenda.model.Evenement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
 
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
 
public class MainView {
 
    // ── DAO & données ────────────────────────────────────────────────────────
    private final EvenementDAO dao  = new EvenementDAO();
    private final ObservableList<Evenement> data = FXCollections.observableArrayList();
 
    // Map date → liste d'événements (pour colorier le calendrier)
    private Map<LocalDate, List<Evenement>> eventsByDate = new HashMap<>();
 
    // ── Couleurs ─────────────────────────────────────────────────────────────
    private static final String RED       = "#e74c3c";
    private static final String DARKRED   = "#c0392b";
    private static final String WHITE     = "#ffffff";
    private static final String OFFWHITE  = "#fafafa";
    private static final String LIGHTGRAY = "#f2f2f7";
    private static final String GRAY      = "#8e8e93";
    private static final String BORDER    = "#e8e8e8";
 
    // Couleur de fond par type d'événement
    private static final Map<String, String> TYPE_COLOR = new LinkedHashMap<>();
    static {
        TYPE_COLOR.put("Reunion",      "#3498db");   // bleu
        TYPE_COLOR.put("Personnel",    "#2ecc71");   // vert
        TYPE_COLOR.put("Anniversaire", "#f39c12");   // orange
        TYPE_COLOR.put("Rappel",       "#e74c3c");   // rouge
        TYPE_COLOR.put("Autre",        "#9b59b6");   // violet
    }
 
    private static final double W = 340;
    private static final double H = 720;
 
    // Vue courante : "calendrier" ou "liste"
    private String currentView = "calendrier";
 
    // Conteneur principal switché
    private StackPane mainContent;
 
    public void start(Stage stage) {
        stage.setTitle("Agenda");
        stage.setResizable(false);
 
        // ── Charger les données ──────────────────────────────────────────────
        chargerDonnees();
        // NAVBAR commune aux deux vues
        Button btnRetour = new Button("‹ Retour");
        btnRetour.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: " + DARKRED + ";" +
            "-fx-font-size: 15; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 8 0 0;");
        btnRetour.setOnAction(e -> new SplashView().start(stage));
 
        Label titleLabel = new Label("Agenda");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web(DARKRED));
 
        Region spacerNav = new Region();
        HBox.setHgrow(spacerNav, Priority.ALWAYS);
 
        // Bouton toggle Calendrier / Événements
        Button btnToggle = new Button("Événements ☰");
        btnToggle.setStyle(
            "-fx-background-color: " + RED + "; -fx-text-fill: white;" +
            "-fx-font-size: 12; -fx-font-weight: bold; -fx-background-radius: 10;" +
            "-fx-padding: 6 12 6 12; -fx-cursor: hand;");
 
        // Bouton + Ajouter
        Button btnAjouter = phoneRedBtn("＋");
 
        HBox navbar = new HBox(8, btnRetour, titleLabel, spacerNav, btnToggle, btnAjouter);
        navbar.setPadding(new Insets(14, 14, 12, 14));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setStyle(
            "-fx-background-color: " + WHITE + ";" +
            "-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        // VUE 1 : CALENDRIER ANNUEL
        ScrollPane calendarScroll = buildCalendarView();
        // VUE 2 : LISTE (ancienne MainView)
        VBox listeView = buildListeView(stage);
 
        // ════════════════════════════════════════════════════════════════════
        // SWITCH entre les deux vues
        // ════════════════════════════════════════════════════════════════════
        mainContent = new StackPane(calendarScroll, listeView);
        listeView.setVisible(false);
 
        btnToggle.setOnAction(e -> {
            if (currentView.equals("calendrier")) {
                currentView = "liste";
                calendarScroll.setVisible(false);
                listeView.setVisible(true);
                btnToggle.setText("Calendrier 📅");
            } else {
                currentView = "calendrier";
                listeView.setVisible(false);
                calendarScroll.setVisible(true);
                btnToggle.setText("Événements ☰");
                // Reconstruire le calendrier pour prendre en compte les nouveaux événements
                calendarScroll.setContent(buildCalendarGrid());
            }
        });
 
        // Bouton ajouter (fonctionne dans les deux vues)
        btnAjouter.setOnAction(e -> {
            new AddEditDialog(null).showAndWait().ifPresent(evt -> {
                try {
                    dao.ajouter(evt);
                    chargerDonnees();
                    calendarScroll.setContent(buildCalendarGrid());
                } catch (SQLException ex) {
                    showError("Erreur", ex.getMessage());
                }
            });
        });
 
        // ── Layout racine ────────────────────────────────────────────────────
        VBox root = new VBox(navbar, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        root.setStyle("-fx-background-color: " + OFFWHITE + ";");
 
        Scene scene = new Scene(root, W, H);
        stage.setScene(scene);
        stage.show();
    }
 
    // ════════════════════════════════════════════════════════════════════════
    // CALENDRIER ANNUEL
    // ════════════════════════════════════════════════════════════════════════
    private ScrollPane buildCalendarView() {
        ScrollPane scroll = new ScrollPane(buildCalendarGrid());
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + OFFWHITE + "; -fx-background: " + OFFWHITE + ";");
        return scroll;
    }
 
    private VBox buildCalendarGrid() {
        int year = LocalDate.now().getYear();
 
        // En-tête de l'année
        Label yearLabel = new Label(String.valueOf(year));
        yearLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        yearLabel.setTextFill(Color.web(RED));
        yearLabel.setPadding(new Insets(12, 16, 4, 16));
 
        VBox wrapper = new VBox(yearLabel);
        wrapper.setStyle("-fx-background-color: " + OFFWHITE + ";");
 
        // Grille 3 colonnes de mois
        int cols = 3;
        for (int row = 0; row < 4; row++) {
            HBox rowBox = new HBox();
            rowBox.setSpacing(0);
            for (int col = 0; col < cols; col++) {
                int monthNum = row * cols + col + 1;
                VBox monthBox = buildMonthBox(YearMonth.of(year, monthNum));
                HBox.setHgrow(monthBox, Priority.ALWAYS);
                monthBox.setMaxWidth(Double.MAX_VALUE);
                rowBox.getChildren().add(monthBox);
            }
            wrapper.getChildren().add(rowBox);
        }
 
        return wrapper;
    }
 
    /** Construit la mini-grille d'un mois */
    private VBox buildMonthBox(YearMonth ym) {
        LocalDate today = LocalDate.now();
        String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        // Majuscule première lettre
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
 
        boolean isCurrentMonth = (ym.getYear() == today.getYear() && ym.getMonth() == today.getMonth());
 
        // Nom du mois
        Label moisLbl = new Label(monthName);
        moisLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        moisLbl.setTextFill(isCurrentMonth ? Color.web(RED) : Color.web("#1c1c1e"));
        moisLbl.setPadding(new Insets(10, 4, 4, 4));
 
        VBox monthBox = new VBox(2, moisLbl);
        monthBox.setPadding(new Insets(0, 2, 8, 8));
        monthBox.setStyle("-fx-background-color: " + OFFWHITE + ";");
 
        // Jours de la semaine (L M M J V S D)
        String[] jours = {"L", "M", "M", "J", "V", "S", "D"};
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
 
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(jours[i]);
            lbl.setFont(Font.font("System", 8));
            lbl.setTextFill(Color.web(GRAY));
            lbl.setPrefWidth(16);
            lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, i, 0);
        }
 
        // Remplissage des jours
        // Jour de la semaine du 1er (1=lundi, 7=dimanche)
        int firstDow = ym.atDay(1).getDayOfWeek().getValue(); // 1=lundi
        int col = firstDow - 1;
        int row = 1;
 
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            List<Evenement> evtsJour = eventsByDate.getOrDefault(date, Collections.emptyList());
 
            StackPane cell = buildDayCell(date, today, evtsJour);
 
            grid.add(cell, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
 
        monthBox.getChildren().add(grid);
        return monthBox;
    }
 
    /** Cellule d'un jour dans le calendrier */
    private StackPane buildDayCell(LocalDate date, LocalDate today, List<Evenement> evts) {
        Label numLbl = new Label(String.valueOf(date.getDayOfMonth()));
        numLbl.setFont(Font.font("System", 9));
        numLbl.setAlignment(Pos.CENTER);
        numLbl.setPrefWidth(16);
        numLbl.setPrefHeight(16);
 
        StackPane cell = new StackPane(numLbl);
        cell.setPrefWidth(16);
        cell.setPrefHeight(16);
 
        if (date.equals(today)) {
            // Aujourd'hui : cercle rouge
            cell.setStyle("-fx-background-color: " + RED + "; -fx-background-radius: 50;");
            numLbl.setTextFill(Color.WHITE);
            numLbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        } else if (!evts.isEmpty()) {
            // A des événements : fond coloré selon type du premier événement
            String color = getColorForType(evts.get(0).getTypeEvt());
            cell.setStyle("-fx-background-color: " + color + "33; -fx-background-radius: 4;");
            numLbl.setTextFill(Color.web(color));
            numLbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        } else {
            numLbl.setTextFill(Color.web("#1c1c1e"));
        }
 
        // Clic → popup avec détails des événements du jour
        if (!evts.isEmpty()) {
            cell.setStyle(cell.getStyle() + "-fx-cursor: hand;");
            cell.setOnMouseClicked(e -> showDayPopup(date, evts));
            cell.setOnMouseEntered(e -> {
                if (!date.equals(today)) {
                    String c = getColorForType(evts.get(0).getTypeEvt());
                    cell.setStyle("-fx-background-color: " + c + "66; -fx-background-radius: 4; -fx-cursor: hand;");
                }
            });
            cell.setOnMouseExited(e -> {
                if (!date.equals(today)) {
                    String c = getColorForType(evts.get(0).getTypeEvt());
                    cell.setStyle("-fx-background-color: " + c + "33; -fx-background-radius: 4; -fx-cursor: hand;");
                }
            });
        }
 
        return cell;
    }
 
    /** Popup des événements d'un jour cliqué */
    private void showDayPopup(LocalDate date, List<Evenement> evts) {
        String dateStr = date.getDayOfMonth() + " " +
            date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + date.getYear();
 
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Événements du " + dateStr);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setStyle("-fx-background-color: " + WHITE + ";");
 
        VBox content = new VBox(10);
        content.setPadding(new Insets(10, 0, 0, 0));
 
        // En-tête date
        Label dateLbl = new Label(dateStr);
        dateLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        dateLbl.setTextFill(Color.web(DARKRED));
        content.getChildren().add(dateLbl);
 
        // Carte par événement
        for (Evenement e : evts) {
            String color = getColorForType(e.getTypeEvt());
 
            VBox card = new VBox(4);
            card.setPadding(new Insets(10, 12, 10, 12));
            card.setStyle(
                "-fx-background-color: " + color + "18;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 0 0 0 4;" +
                "-fx-background-radius: 6;");
 
            // Titre + heure
            HBox top = new HBox(8);
            Label titreLbl = new Label(e.getTitre());
            titreLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
            titreLbl.setTextFill(Color.web("#1c1c1e"));
 
            Label heureLbl = new Label(e.getHeure() != null && !e.getHeure().isBlank()
                ? "🕐 " + e.getHeure() : "");
            heureLbl.setFont(Font.font("System", 12));
            heureLbl.setTextFill(Color.web(GRAY));
 
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            top.getChildren().addAll(titreLbl, sp, heureLbl);
 
            // Badge type
            Label typeLbl = new Label(e.getTypeEvt());
            typeLbl.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white; -fx-font-size: 10;" +
                "-fx-background-radius: 8; -fx-padding: 2 8 2 8;");
 
            card.getChildren().addAll(top, typeLbl);
 
            // Description
            if (e.getDescription() != null && !e.getDescription().isBlank()) {
                Label descLbl = new Label(e.getDescription());
                descLbl.setFont(Font.font("System", 12));
                descLbl.setTextFill(Color.web(GRAY));
                descLbl.setWrapText(true);
                card.getChildren().add(descLbl);
            }
 
            content.getChildren().add(card);
        }
 
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color: " + WHITE + "; -fx-background: " + WHITE + ";");
        sp.setPrefHeight(Math.min(evts.size() * 95 + 50, 340));
        sp.setPrefWidth(290);
 
        dlg.getDialogPane().setContent(sp);
        dlg.showAndWait();
    }
 
    // ════════════════════════════════════════════════════════════════════════
    // VUE LISTE (ancienne MainView, intégrée ici)
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildListeView(Stage stage) {
        TableView<Evenement> table = new TableView<>(data);
        table.setPlaceholder(new Label("Aucun événement."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: " + WHITE + ";" +
            "-fx-selection-bar: #fdecea; -fx-font-size: 12;");
 
        TableColumn<Evenement, String>    colTitre = makeCol("Titre",  "titre",  130);
        TableColumn<Evenement, LocalDate> colDate  = makeCol("Date",   "date",    88);
        TableColumn<Evenement, String>    colHeure = makeCol("Heure",  "heure",   52);
        TableColumn<Evenement, String>    colType  = makeCol("Type",   "typeEvt", 90);
        TableColumn<Evenement, String>    colDesc  = makeCol("Description", "description", 0);
        table.getColumns().addAll(colTitre, colDate, colHeure, colType, colDesc);
 
        // CRUD bar
        Button btnModifier   = phoneOutlineBtn("✎ Modifier");
        Button btnSupprimer  = phoneOutlineBtn("✕ Supprimer");
        Button btnActualiser = phoneGhostBtn("↺");
        HBox.setHgrow(btnModifier, Priority.ALWAYS);
        HBox.setHgrow(btnSupprimer, Priority.ALWAYS);
        HBox crudBar = new HBox(6, btnModifier, btnSupprimer, btnActualiser);
        crudBar.setPadding(new Insets(10, 14, 10, 14));
        crudBar.setStyle("-fx-background-color: " + WHITE + ";");
 
        // Filtres
        TextField titreFilter = new TextField();
        titreFilter.setPromptText("🔍  Rechercher par titre...");
        titreFilter.setMaxWidth(Double.MAX_VALUE);
        stylePhoneField(titreFilter);
 
        DatePicker dateFilter = new DatePicker();
        dateFilter.setPromptText("Date");
        dateFilter.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(dateFilter, Priority.ALWAYS);
        dateFilter.setStyle("-fx-background-color:" + WHITE + "; -fx-border-color:" + BORDER +
            "; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:12;");
 
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous","Reunion","Personnel","Anniversaire","Rappel","Autre");
        typeFilter.setValue("Tous");
        typeFilter.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(typeFilter, Priority.ALWAYS);
        typeFilter.setStyle("-fx-background-color:" + WHITE + "; -fx-border-color:" + BORDER +
            "; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:12;");
 
        HBox row2 = new HBox(6, dateFilter, typeFilter);
        Button btnSearch = phoneRedBtn("Filtrer");
        Button btnReset  = phoneGhostBtn("Effacer");
        Button btnSecret = phoneSecretBtn("🔒 Privé");
        HBox.setHgrow(btnSearch, Priority.ALWAYS);
        HBox.setHgrow(btnReset,  Priority.ALWAYS);
        HBox.setHgrow(btnSecret, Priority.ALWAYS);
        HBox row3 = new HBox(6, btnSearch, btnReset, btnSecret);
 
        VBox filterBar = new VBox(8, titreFilter, row2, row3);
        filterBar.setPadding(new Insets(10, 14, 10, 14));
        filterBar.setStyle("-fx-background-color:" + LIGHTGRAY + "; -fx-border-color:" + BORDER +
            "; -fx-border-width:1 0 1 0;");
 
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 11));
        statusLabel.setTextFill(Color.web(GRAY));
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(6, 14, 8, 14));
        statusBar.setStyle("-fx-background-color:" + OFFWHITE +
            "; -fx-border-color:" + BORDER + "; -fx-border-width:1 0 0 0;");
 
        VBox listeView = new VBox(crudBar, filterBar, table, statusBar);
        VBox.setVgrow(table, Priority.ALWAYS);
        listeView.setStyle("-fx-background-color:" + OFFWHITE + ";");
 
        // Actions CRUD
        btnModifier.setOnAction(e -> {
            Evenement sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Sélectionnez un événement."); return; }
            new AddEditDialog(sel).showAndWait().ifPresent(evt -> {
                try { dao.modifier(evt); chargerDonnees(); statusLabel.setText("Modifié ✓"); }
                catch (SQLException ex) { showError("Erreur", ex.getMessage()); }
            });
        });
 
        btnSupprimer.setOnAction(e -> {
            Evenement sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Sélectionnez un événement."); return; }
            Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + sel.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(b -> {
                if (b == ButtonType.YES) {
                    try { dao.supprimer(sel.getId()); chargerDonnees(); statusLabel.setText("Supprimé ✓"); }
                    catch (SQLException ex) { showError("Erreur", ex.getMessage()); }
                }
            });
        });
 
        btnActualiser.setOnAction(e -> { chargerDonnees(); statusLabel.setText("Actualisé ✓"); });
 
        btnSearch.setOnAction(e -> {
            try {
                String titre = titreFilter.getText().trim();
                LocalDate date = dateFilter.getValue();
                String type = typeFilter.getValue();
                List<Evenement> res = dao.rechercher(titre.isEmpty() ? null : titre, date, type, false);
                data.setAll(res);
                statusLabel.setText(res.size() + " résultat(s)");
            } catch (SQLException ex) { showError("Erreur", ex.getMessage()); }
        });
 
        btnReset.setOnAction(e -> {
            titreFilter.clear(); dateFilter.setValue(null); typeFilter.setValue("Tous");
            chargerDonnees(); statusLabel.setText("Filtres effacés");
        });
 
        btnSecret.setOnAction(e -> {
            if (demanderCodeSecret()) {
                try {
                    String titre = titreFilter.getText().trim();
                    List<Evenement> secrets = dao.rechercher(
                        titre.isEmpty() ? null : titre,
                        dateFilter.getValue(), typeFilter.getValue(), true);
                    data.setAll(secrets);
                    statusLabel.setText("Privé : " + secrets.size() + " résultat(s)");
                } catch (SQLException ex) { showError("Erreur", ex.getMessage()); }
            }
        });
 
        return listeView;
    }
 
    // ════════════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ════════════════════════════════════════════════════════════════════════
 
    /** Charge les événements depuis la BDD et reconstruit la map date→événements */
    private void chargerDonnees() {
        try {
            List<Evenement> tous = dao.listerTous();
            data.setAll(tous);
            eventsByDate = tous.stream()
                .filter(e -> e.getDate() != null)
                .collect(Collectors.groupingBy(Evenement::getDate));
        } catch (SQLException ex) {
            showError("Erreur chargement", ex.getMessage());
        }
    }
 
    private String getColorForType(String type) {
        return TYPE_COLOR.getOrDefault(type, "#8e8e93");
    }
 
    private boolean demanderCodeSecret() {
        final String CODE = "2310";
        Dialog<String> dlg = new Dialog<>();
        dlg.setTitle("Zone Protégée");
        dlg.setHeaderText("Code secret requis");
        ButtonType ok = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        PasswordField pf = new PasswordField();
        pf.setPromptText("Code");
        VBox box = new VBox(10, pf);
        box.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(box);
        javafx.scene.Node okNode = dlg.getDialogPane().lookupButton(ok);
        okNode.setDisable(true);
        pf.textProperty().addListener((o, a, b) -> okNode.setDisable(b.trim().isEmpty()));
        dlg.setResultConverter(b -> b == ok ? pf.getText().trim() : null);
        Optional<String> res = dlg.showAndWait();
        if (res.isPresent() && res.get().equals(CODE)) return true;
        if (res.isPresent())
            new Alert(Alert.AlertType.ERROR, "Code incorrect.", ButtonType.OK).showAndWait();
        return false;
    }
 
    private void chargerDonneesEtRafraichirCal(ScrollPane calScroll) {
        chargerDonnees();
        calScroll.setContent(buildCalendarGrid());
    }
 
    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> makeCol(String label, String prop, double width) {
        TableColumn<S, T> c = new TableColumn<>(label);
        c.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(prop));
        if (width > 0) c.setPrefWidth(width);
        return c;
    }
 
    private void stylePhoneField(TextField f) {
        f.setStyle("-fx-background-color:" + WHITE + "; -fx-border-color:" + BORDER +
            "; -fx-border-radius:10; -fx-background-radius:10; -fx-padding:6 12 6 12; -fx-font-size:13;");
    }
 
    private Button phoneRedBtn(String txt) {
        Button b = new Button(txt);
        String s = "-fx-background-color:" + RED + "; -fx-text-fill:white; -fx-font-weight:bold;" +
            "-fx-font-size:13; -fx-background-radius:10; -fx-padding:8 16 8 16; -fx-cursor:hand;";
        String h = "-fx-background-color:" + DARKRED + "; -fx-text-fill:white; -fx-font-weight:bold;" +
            "-fx-font-size:13; -fx-background-radius:10; -fx-padding:8 16 8 16; -fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
 
    private Button phoneOutlineBtn(String txt) {
        Button b = new Button(txt);
        String s = "-fx-background-color:" + WHITE + "; -fx-text-fill:" + DARKRED + ";" +
            "-fx-border-color:" + DARKRED + "; -fx-border-width:1.5;" +
            "-fx-border-radius:10; -fx-background-radius:10;" +
            "-fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 12 8 12; -fx-cursor:hand;";
        String h = "-fx-background-color:#fdecea; -fx-text-fill:" + DARKRED + ";" +
            "-fx-border-color:" + DARKRED + "; -fx-border-width:1.5;" +
            "-fx-border-radius:10; -fx-background-radius:10;" +
            "-fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 12 8 12; -fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
 
    private Button phoneGhostBtn(String txt) {
        Button b = new Button(txt);
        String s = "-fx-background-color:" + LIGHTGRAY + "; -fx-text-fill:" + GRAY + ";" +
            "-fx-background-radius:10; -fx-font-size:13; -fx-padding:8 14 8 14; -fx-cursor:hand;";
        String h = "-fx-background-color:#e5e5ea; -fx-text-fill:" + GRAY + ";" +
            "-fx-background-radius:10; -fx-font-size:13; -fx-padding:8 14 8 14; -fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
 
    private Button phoneSecretBtn(String txt) {
        Button b = new Button(txt);
        String s = "-fx-background-color:#fff0f0; -fx-text-fill:" + DARKRED + ";" +
            "-fx-border-color:" + DARKRED + "; -fx-border-width:1.5;" +
            "-fx-border-radius:10; -fx-background-radius:10;" +
            "-fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 12 8 12; -fx-cursor:hand;";
        String h = "-fx-background-color:#fde0e0; -fx-text-fill:" + DARKRED + ";" +
            "-fx-border-color:" + DARKRED + "; -fx-border-width:1.5;" +
            "-fx-border-radius:10; -fx-background-radius:10;" +
            "-fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 12 8 12; -fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
 
    private void showError(String t, String m) {
        new Alert(Alert.AlertType.ERROR, m).showAndWait();
    }
 
    private void showInfo(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }
}