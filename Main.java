/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agenda;
import agenda.ui.SplashView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author DELL
 */
public class Main extends Application{
       @Override
    public void start(Stage primaryStage) {
        new SplashView().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
