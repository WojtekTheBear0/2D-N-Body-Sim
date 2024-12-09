package nbody;

import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nbody.PhysicsEngine.VerletObject;
import nbody.PhysicsEngine.simulation;
import nbody.gui.Maingui;

public class Main extends Application {
    private static final int WIDTH = 750;
    private static final int HEIGHT = 750;
    private static final int NUM_BODIES = 4000;
    
    private simulation sim;
    private long lastTime = 0;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Maingui gui = new Maingui();

        Scene scene2 = new Scene(gui.getGrid(), 400, 600);
        Stage secondaryStage = new Stage();
        secondaryStage.setTitle("Simulation Controls");
        secondaryStage.setScene(scene2);

        // Initialize simulation
        sim = new simulation(WIDTH,HEIGHT);
        //sim.setBoundaries(0, WIDTH, 0, HEIGHT);
        sim.setSimuationUpdateRate(60);  // 60 FPS
        sim.setSubStepsCount(1);         // 8 substeps for stability
       // sim.setGravity(new Point2D(0, 20.81));  // Same gravity as original
        
       Point2D spawnPos = new Point2D(10, 10);

        // initializeObjects();

        System.out.println("Number of objects initialized: " + sim.getSystemManager().getObjectCount());

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate delta time
                //Get rid of this 
                float dt;
                if (lastTime == 0) {
                    dt = 0.016f; // Initial default timestep (roughly 60 FPS)
                } else {
                    dt = (float)((now - lastTime) / 1e9); // Convert nanoseconds to seconds
                }
                lastTime = now;
                
                // End  
                sim.addStream(spawnPos,5);
                // Clear canvas
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WIDTH, HEIGHT);

                // Update physics
                sim.update();

                // Draw objects
                gc.setFill(Color.SKYBLUE);
                for (VerletObject obj : sim.getSystemManager().getObjects()) {
                    Point2D pos = obj.getPosition();
                    float radius = obj.getRadius();
                    gc.fillOval(pos.getX() - radius, pos.getY() - radius, radius * 2, radius * 2);
                }
            }
        };

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("N-Body Simulation with Verlet Integration");
        primaryStage.setScene(scene);
        primaryStage.show();
        secondaryStage.show();
        timer.start();
    }


    //Not in use ATM
    private void initializeObjects() {
        Random rand = new Random();
        for (int i = 0; i < NUM_BODIES; i++) {
            double x = rand.nextDouble() * WIDTH;
            double y = rand.nextDouble() * HEIGHT;
            Point2D position = new Point2D(x, y);
            
            // Create VerletObject with random position, radius, and mass
            VerletObject obj = new VerletObject(position, 5f, 5e10f);
            
            // Set initial velocity
            Point2D velocity = new Point2D(
                (rand.nextDouble() - 0.5) * 4,
                (rand.nextDouble() - 0.5) * 4
            );
            obj.SetVelocity(velocity, 0.016f);
            
            // Add object to simulation
            sim.addObject(obj);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}