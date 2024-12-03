package nbody;


import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import nbody.PhysicsEngine.VerletObject;
import nbody.gui.Maingui;

public class Main extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_BODIES = 25;
    private static final double G = 6.67430e-11; // Gravitational constant (scaled for simulation)
    private static final double MASS = 5e10; // Mass of each object (scaled for simulation)
    private static final double TIME_STEP = 1; // Time step for integration

    private final Body[] bodies = new Body[NUM_BODIES];

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Maingui gui = new Maingui();

        Scene scene2 = new Scene(gui.getGrid(), 400, 600);
        Stage secondaryStage = new Stage(); // New Stage for scene2
        secondaryStage.setTitle("Simulation Controls");
        secondaryStage.setScene(scene2);



        initializeBodies();

        // Animation timer
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Clear canvas
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WIDTH, HEIGHT);

                // Update positions using Verlet integration
                updateBodies();

                // Draw bodies
                gc.setFill(Color.SKYBLUE);
                for (Body body : bodies) {
                    gc.fillOval(body.x - body.radius, body.y - body.radius, body.radius * 2, body.radius * 2);
                }
            }
        };

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("N-Body Simulation with Verlet Integration");
        primaryStage.setScene(scene);
        primaryStage.show();


        secondaryStage.show();
        // Start animation
        timer.start();
    }

    private void initializeBodies() {
        Random rand = new Random();
        for (int i = 0; i < NUM_BODIES; i++) {
            double x = rand.nextDouble() * WIDTH;
            double y = rand.nextDouble() * HEIGHT;
            double vx = (rand.nextDouble() - 0.5) * 4;
            double vy = (rand.nextDouble() - 0.5) * 4;

            bodies[i] = new Body(x, y, vx, vy, MASS, 5);
        }
    }

    private void updateBodies() {
        for (Body body : bodies) {
            body.resetAcceleration();
        }

        // Calculate gravitational forces
        for (int i = 0; i < NUM_BODIES; i++) {
            for (int j = i + 1; j < NUM_BODIES; j++) {
                Body b1 = bodies[i];
                Body b2 = bodies[j];

                double dx = b2.x - b1.x;
                double dy = b2.y - b1.y;
                double distance = Math.sqrt(dx * dx + dy * dy) + 1e-3; // Avoid division by zero
                double force = (G * b1.mass * b2.mass) / (distance * distance);
                double ax = force * dx / (distance * b1.mass);
                double ay = force * dy / (distance * b1.mass);
                double bx = -force * dx / (distance * b2.mass);
                double by = -force * dy / (distance * b2.mass);

                b1.ax += ax;
                b1.ay += ay;
                b2.ax += bx;
                b2.ay += by;
            }
        }

        // Update positions using Verlet integration
        for (Body body : bodies) {
            double newX = 2 * body.x - body.prevX + body.ax * TIME_STEP * TIME_STEP;
            double newY = 2 * body.y - body.prevY + body.ay * TIME_STEP * TIME_STEP;

            body.prevX = body.x;
            body.prevY = body.y;

            body.x = newX;
            body.y = newY;

            // Handle boundary collisions
            if (body.x < 0 || body.x > WIDTH) {
                body.vx = -body.vx;
                body.x = Math.max(0, Math.min(WIDTH, body.x));
            }
            if (body.y < 0 || body.y > HEIGHT) {
                body.vy = -body.vy;
                body.y = Math.max(0, Math.min(HEIGHT, body.y));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class Body {
        double x, y; // Current position
        double prevX, prevY; // Previous position
        double vx, vy; // Velocity
        double ax, ay; // Acceleration
        double mass; // Mass
        double radius; // Radius for drawing

        public Body(double x, double y, double vx, double vy, double mass, double radius) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.mass = mass;
            this.radius = radius;
            this.prevX = x - vx; // Initialize previous position based on velocity
            this.prevY = y - vy;
        }

        public void resetAcceleration() {
            ax = 0;
            ay = 0;
        }
    }
}
