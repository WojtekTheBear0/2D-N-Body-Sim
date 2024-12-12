package nbody.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SimulationTypeMenu {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public void show(Stage primaryStage, Runnable onCollisionSimulation, Runnable onNBodySimulation) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        // Title
        Text titleText = new Text("Choose Simulation Type");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        titleText.setFill(Color.SKYBLUE);

        // Collision Simulation Button
        Button collisionButton = createStyledButton("Collision Simulation", 
            "-fx-background-color: linear-gradient(to bottom, #ff6b6b, #c0392b);");
        collisionButton.setOnAction(e -> {
            onCollisionSimulation.run();
        });

        // N-Body Forces Simulation Button
        Button nBodyButton = createStyledButton("N-Body Forces Simulation", 
            "-fx-background-color: linear-gradient(to bottom, #4eb5ff, #2980b9);");
        nBodyButton.setOnAction(e -> {
            onNBodySimulation.run();
        });

        layout.getChildren().addAll(titleText, collisionButton, nBodyButton);

        Scene scene = new Scene(layout, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation Type Selection");
        primaryStage.show();
    }

    private Button createStyledButton(String text, String backgroundStyle) {
        Button button = new Button(text);
        button.setStyle(
            backgroundStyle +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);"
        );

        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle(
            backgroundStyle +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 0);"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            backgroundStyle +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);"
        ));

        return button;
    }
}