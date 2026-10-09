/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agenda.ui;

import agenda.dao.EvenementDAO;
import agenda.model.Evenement;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
/**
 *
 * @author DELL
 */
public class SecretView extends Stage {
     public SecretView(EvenementDAO dao, ObservableList<Evenement> mainData) {
        setTitle("Rechercher des evenements");
        initModality(Modality.APPLICATION_MODAL);

        // ---- Search fields ----
        TextField titreField = new TextField();
        titreField.setPromptText("Titre (partiel)");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Tous", "Reunion", "Personnel",
                                    "Anniversaire", "Rappel", "Autre");
        typeCombo.setValue("Tous");

        Button btnSearch = new Button("Rechercher");
        Button btnReset  = new Button("Reinitialiser");
        Button btnClose  = new Button("Fermer");

        HBox fields = new HBox(10,
            new Label("Titre:"), titreField,
            new Label("Date:"), datePicker,
            new Label("Type:"), typeCombo,
            btnSearch, btnReset
        );
        fields.setPadding(new Insets(10));

        // ---- Results table ----
        TableView<Evenement> resultTable = new TableView<>();
        resultTable.setPlaceholder(new Label("Aucun resultat."));

        TableColumn<Evenement, String>    colTitre = new TableColumn<>("Titre");
        TableColumn<Evenement, LocalDate> colDate  = new TableColumn<>("Date");
        TableColumn<Evenement, String>    colHeure = new TableColumn<>("Heure");
        TableColumn<Evenement, String>    colType  = new TableColumn<>("Type");
        TableColumn<Evenement, String>    colDesc  = new TableColumn<>("Description");

        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvt"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        colTitre.setPrefWidth(150);
        colDate.setPrefWidth(85);
        colHeure.setPrefWidth(60);
        colType.setPrefWidth(85);
        colDesc.setPrefWidth(230);

        resultTable.getColumns().addAll(colTitre, colDate, colHeure, colType, colDesc);

        Label countLabel = new Label();

        HBox bottomBar = new HBox(10, countLabel, new Region(), btnClose);
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        bottomBar.setPadding(new Insets(8, 10, 8, 10));

        VBox root = new VBox(fields, resultTable, bottomBar);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        // ---- Actions ----
        btnSearch.setOnAction(e -> {
            try {
                String titre = titreField.getText().trim();
                LocalDate date = datePicker.getValue();
                String type = typeCombo.getValue();
                       List<Evenement> results = dao.rechercher(titre.isEmpty() ? null : titre, date, type, false);
                resultTable.getItems().setAll(results);
                countLabel.setText(results.size() + " resultat(s) trouve(s).");
                // Also refresh main window data
                mainData.setAll(dao.listerTous());
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });

        btnReset.setOnAction(e -> {
            titreField.clear();
            datePicker.setValue(null);
            typeCombo.setValue("Tous");
            resultTable.getItems().clear();
            countLabel.setText("");
        });

        btnClose.setOnAction(e -> close());

        setScene(new Scene(root, 700, 400));
    }
}