package nbody.gui;


import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartMenu {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private List<StarParticle> stars = new ArrayList<>();
    private Random random = new Random();

    public void show(Stage primaryStage, Runnable onStartClicked) {
        StackPane root = new StackPane();
        Canvas backgroundCanvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        // Create stars for background animation
        initializeStars();

        // Star animation timer
        AnimationTimer starTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Clear the background
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WIDTH, HEIGHT);

                // Draw and update stars
                for (StarParticle star : stars) {
                    star.update();
                    star.draw(gc);
                }
            }
        };

        // Title Label
        javafx.scene.text.Text titleText = new javafx.scene.text.Text("N-BODY SIMULATION");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        titleText.setFill(Color.SKYBLUE);

        // Subtitle
        javafx.scene.text.Text subtitleText = new javafx.scene.text.Text("Explore Gravitational Dynamics");
        subtitleText.setFont(Font.font("Arial", FontWeight.LIGHT, 20));
        subtitleText.setFill(Color.LIGHTGRAY);

        // Start Button
        Button startButton = new Button("Launch Simulation");
        startButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4eb5ff, #2980b9);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);"
        );

        // Hover effects
        startButton.setOnMouseEntered(e -> startButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #5fc1ff, #3498db);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 0);"
        ));
        startButton.setOnMouseExited(e -> startButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4eb5ff, #2980b9);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);"
        ));

        startButton.setOnAction(e -> {
            starTimer.stop();
            onStartClicked.run();
        });

        // Layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleText, subtitleText, startButton);

        // Add layers to StackPane
        root.getChildren().addAll(backgroundCanvas, layout);

        // Create scene
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("N-Body Simulation");
        primaryStage.show();

        starTimer.start();
    }

    private void initializeStars() {
        for (int i = 0; i < 200; i++) {
            stars.add(new StarParticle(
                random.nextDouble() * WIDTH, 
                random.nextDouble() * HEIGHT
            ));
        }
    }

    // Inner class for star particles
    private class StarParticle {
        private double x, y;
        private double size;
        private double speed;
        private Color color;

        public StarParticle(double x, double y) {
            this.x = x;
            this.y = y;
            this.size = random.nextDouble() * 2 + 0.5;
            this.speed = random.nextDouble() * 0.5 + 0.1;
            this.color = Color.color(1, 1, 1, random.nextDouble() * 0.7 + 0.3);
        }

        public void update() {
            y += speed;
            if (y > HEIGHT) {
                y = 0;
                x = random.nextDouble() * WIDTH;
            }
        }

        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x, y, size, size);
        }
    }
}