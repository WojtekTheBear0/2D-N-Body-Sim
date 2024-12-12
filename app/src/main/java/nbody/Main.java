package nbody;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nbody.PhysicsEngine.CollisionManager;
import nbody.PhysicsEngine.GravityManager;
import nbody.PhysicsEngine.VerletObject;
import nbody.gui.Maingui;
import nbody.gui.StartMenu;
import nbody.gui.SimulationTypeMenu;

public class Main extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private Vector<VerletObject> objects = new Vector<>();
    private CollisionManager collisionManager;
    private long lastTime = 0;

    @Override
    public void start(Stage primaryStage) {
        StartMenu startMenu = new StartMenu();
        
        startMenu.show(primaryStage, () -> {
            SimulationTypeMenu simulationTypeMenu = new SimulationTypeMenu();
            simulationTypeMenu.show(primaryStage, 
                () -> {
                    // Collision Simulation Logic (Placeholder)
                    System.out.println("Collision Simulation Selected");
                },
                () -> {
                    // N-Body Forces Simulation
                    Maingui gui = new Maingui();

                    gui.setOnRun(values -> {
                        Canvas canvas = new Canvas(WIDTH, HEIGHT);
                        GraphicsContext gc = canvas.getGraphicsContext2D();

                        initializeObjects(values.getObjectCount(), values.getMass(), values.getMassVariance(),
                                values.getDiameter(), values.getDiameterVariance());
                        collisionManager = new CollisionManager(objects);

                        AnimationTimer timer = new AnimationTimer() {
                            private double elapsedTime = 0;
                            private BufferedWriter singleWriter;
                            private BufferedWriter duoWriter;
                            private BufferedWriter allWriter;

                            {
                                try {
                                    if (values.getSingleTrackObject() != null) {
                                        singleWriter = new BufferedWriter(new FileWriter("single_object_tracking.txt"));
                                        singleWriter.write("Mass: " + objects.get(values.getSingleTrackObject()).getMass() + " kg\n");
                                        singleWriter.write("Diameter: " + objects.get(values.getSingleTrackObject()).getRadius() * 2 + " m\n\n");
                                        singleWriter.write("Time (s)\tAcceleration (m/s²)\tVelocity (m/s)\tPosition (m, m)\t\tForces felt (MN)\n");
                                    } 
                                    // ... rest of the file writing logic remains the same
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void handle(long now) {
                                // Existing simulation logic
                                float dt;
                                if (lastTime == 0) {
                                    dt = 0.016f;
                                } else {
                                    dt = (float)((now - lastTime) / 1e9);
                                }
                                lastTime = now;
                                elapsedTime += dt;

                                if (elapsedTime > values.getTimeToTrack()) {
                                    stop();
                                    return; 
                                }
                                
                                gc.setFill(Color.BLACK);
                                gc.fillRect(0, 0, WIDTH, HEIGHT);

                                updateObjects(dt);

                                if (values.useBruteForce()) {
                                    collisionManager.BruteForceSolve();
                                    GravityManager.computeForcesBruteForce(objects);
                                } else {
                                    collisionManager.quadTreeCollision();
                                    GravityManager.computeForcesWithQuadTree(objects);
                                }

                                gc.setFill(Color.SKYBLUE);
                                for (VerletObject obj : objects) {
                                    Point2D pos = obj.getPosition();
                                    float radius = obj.getRadius();
                                    gc.fillOval(pos.getX() - radius, pos.getY() - radius, radius * 2, radius * 2);
                                }

                                // ... rest of the file tracking logic remains the same
                            }

                            @Override
                            public void stop() {
                                try {
                                    if (singleWriter != null) singleWriter.close();
                                    if (duoWriter != null) duoWriter.close();
                                    if (allWriter != null) allWriter.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                super.stop();
                            }
                        };

                        StackPane root = new StackPane(canvas);
                        Scene scene = new Scene(root, WIDTH, HEIGHT);

                        primaryStage.setTitle("N-Body Simulation with Verlet Integration");
                        primaryStage.setScene(scene);
                        primaryStage.show();
                        timer.start();

                        gui.close();
                    });

                    Scene guiScene = new Scene(gui.getGrid(), 300, 475); 
                    primaryStage.setTitle("N-Body Configuration");
                    primaryStage.setScene(guiScene);
                    primaryStage.setResizable(false);
                    primaryStage.show();
                }
            );
        });
    }

    private void initializeObjects(int numBodies, double mass, double massVariance, double diameter, double diameterVariance) {
        Random rand = new Random();
        for (int i = 0; i < numBodies; i++) {
            double x = rand.nextDouble() * WIDTH;
            double y = rand.nextDouble() * HEIGHT;
            Point2D position = new Point2D(x, y);

            double randomMass = (mass + (rand.nextDouble() - 0.5) * massVariance) * 1e12;
            double randomDiameter = diameter + (rand.nextDouble() - 0.5) * diameterVariance;

            VerletObject obj = new VerletObject(position, (float) randomDiameter / 2, (float) randomMass);
            objects.add(obj);
        }
    }

    private void updateObjects(float dt) {
        for (VerletObject obj : objects) {
            obj.update(dt);
            
            Point2D pos = obj.getPosition();
            Point2D newPos = new Point2D(
                Math.max(obj.getRadius(), Math.min(WIDTH - obj.getRadius(), pos.getX())),
                Math.max(obj.getRadius(), Math.min(HEIGHT - obj.getRadius(), pos.getY()))
            );
     
            obj.SetPosition(newPos);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}