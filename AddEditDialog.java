/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agenda.ui;

import agenda.model.Evenement;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

/**
 *
 * @author DELL
 */
public class AddEditDialog extends Dialog<Evenement> {
   private static final String[] TYPES = {"Reunion", "Personnel", "Anniversaire", "Rappel", "Autre"};

    public AddEditDialog(Evenement existing) {
        setTitle(existing == null ? "Ajouter un evenement" : "Modifier l'evenement");
        setHeaderText(null);

        getDialogPane().setStyle("-fx-background-color: #ffffff;");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titreField       = new TextField();
        DatePicker datePicker      = new DatePicker(LocalDate.now());
        TextField heureField       = new TextField();
        heureField.setPromptText("ex: 14:30");
        TextArea descArea          = new TextArea();
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TYPES);
        typeCombo.setValue(TYPES[0]);
        CheckBox secretCheck       = new CheckBox("Evenement secret");

        if (existing != null) {
            titreField.setText(existing.getTitre());
            datePicker.setValue(existing.getDate());
            heureField.setText(existing.getHeure());
            descArea.setText(existing.getDescription());
            typeCombo.setValue(existing.getTypeEvt());
            secretCheck.setSelected(existing.isSecret());
        }

        String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #c0392b;";

        Label lTitre = new Label("Titre:");       lTitre.setStyle(labelStyle);
        Label lDate  = new Label("Date:");        lDate.setStyle(labelStyle);
        Label lHeure = new Label("Heure:");       lHeure.setStyle(labelStyle);
        Label lDesc  = new Label("Description:"); lDesc.setStyle(labelStyle);
        Label lType  = new Label("Type:");        lType.setStyle(labelStyle);

        grid.add(lTitre, 0, 0); grid.add(titreField,  1, 0);
        grid.add(lDate,  0, 1); grid.add(datePicker,  1, 1);
        grid.add(lHeure, 0, 2); grid.add(heureField,  1, 2);
        grid.add(lDesc,  0, 3); grid.add(descArea,    1, 3);
        grid.add(lType,  0, 4); grid.add(typeCombo,   1, 4);
        grid.add(secretCheck, 1, 5);

        getDialogPane().setContent(grid);

        Node saveNode = getDialogPane().lookupButton(saveBtn);
        saveNode.setDisable(true);
        titreField.textProperty().addListener((obs, o, n) ->
            saveNode.setDisable(n.trim().isEmpty()));

        setResultConverter(btn -> {
            if (btn == saveBtn) {
                Evenement e = (existing != null) ? existing : new Evenement();
                e.setTitre(titreField.getText().trim());
                e.setDate(datePicker.getValue());
                e.setHeure(heureField.getText().trim());
                e.setDescription(descArea.getText().trim());
                e.setTypeEvt(typeCombo.getValue());
                e.setSecret(secretCheck.isSelected());
                return e;
            }
            return null;
        });
    }
}