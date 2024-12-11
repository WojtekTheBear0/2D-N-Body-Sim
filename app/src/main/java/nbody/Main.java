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

public class Main extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    
    private Vector<VerletObject> objects = new Vector<>();
    private CollisionManager collisionManager;
    private long lastTime = 0;


    @Override
    public void start(Stage primaryStage) {
        Maingui gui = new Maingui();

        gui.setOnRun(values -> {
            Canvas canvas = new Canvas(WIDTH, HEIGHT);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            initializeObjects(values.getObjectCount(), values.getMass(), values.getMassVariance(),
                    values.getDiameter(), values.getDiameterVariance());
            collisionManager = new CollisionManager(objects);

            AnimationTimer timer = new AnimationTimer() {
                private double elapsedTime = 0; // Track simulation time
                private BufferedWriter singleWriter;
                private BufferedWriter duoWriter;
                private BufferedWriter allWriter;

                {
                    // Initialize file writer based on the selected tracking option
                    try {
                        if (values.getSingleTrackObject() != null) {
                            singleWriter = new BufferedWriter(new FileWriter("single_object_tracking.txt"));
                            singleWriter.write("Mass: " + objects.get(values.getSingleTrackObject()).getMass() + " kg\n");
                            singleWriter.write("Diameter: " + objects.get(values.getSingleTrackObject()).getRadius() * 2 + " m\n\n");
                            singleWriter.write("Time (s)\tAcceleration (m/s²)\tVelocity (m/s)\tPosition (m, m)\t\tForces felt (MN)\n");
                        } 
                        if (values.getRelationshipObject1() != null) {
                            duoWriter = new BufferedWriter(new FileWriter("two_object_relationship_tracking.txt"));
                            duoWriter.write("Mass 1: " + objects.get(values.getRelationshipObject1()).getMass() + " kg\n");
                            duoWriter.write("Diameter 1: " + objects.get(values.getRelationshipObject1()).getRadius() * 2 + " m\n");
                            duoWriter.write("Mass 2: " + objects.get(values.getRelationshipObject2()).getMass() + " kg\n");
                            duoWriter.write("Diameter 2: " + objects.get(values.getRelationshipObject2()).getRadius() * 2 + " m\n\n");
                            duoWriter.write("Time (s)\tDistance (m)\tGravitational Force (MN)\n");
                        } 
                        if (values.isTrackAll()) {
                            allWriter = new BufferedWriter(new FileWriter("all_object_tracking.txt"));
                            allWriter.write("Number of objects: " + objects.size() + "\n\n");
                            allWriter.write("Time (s)     Force(MN)");
                            allWriter.write("\n\n");
                        }
                    } 
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                } 

                @Override
                public void handle(long now) {
                    // Calculate delta time
                    float dt;
                    if (lastTime == 0) {
                        dt = 0.016f; // Initial default timestep (roughly 60 FPS)
                    } else {
                        dt = (float)((now - lastTime) / 1e9); // Convert nanoseconds to seconds
                    }
                    lastTime = now;
                    elapsedTime += dt;

                     
                    // Stop tracking if time exceeds the specified limit
                    if (elapsedTime > values.getTimeToTrack()) {
                        stop();
                        return; 
                    }
                    

                    // Clear canvas
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, WIDTH, HEIGHT);

                    // Update physics
                    updateObjects(dt);

                    if (values.useBruteForce()) {
                        collisionManager.BruteForceSolve();
                        GravityManager.computeForcesBruteForce(objects);
                    }
                    else {
                        collisionManager.quadTreeCollision();
                        //GravityManager.computeForcesWithQuadTree(objects);
                        GravityManager.computeForcesBruteForce(objects);
                    }

                    // Draw objects
                    gc.setFill(Color.SKYBLUE);
                    for (VerletObject obj : objects) {
                        Point2D pos = obj.getPosition();
                        float radius = obj.getRadius();
                        gc.fillOval(pos.getX() - radius, pos.getY() - radius, radius * 2, radius * 2);
                    }

                    
                    // Write tracking data
                    try {
                        if (values.getSingleTrackObject() != null) {
                            VerletObject obj = objects.get(0); // Assuming the first object is tracked
                            singleWriter.write(String.format("%.2f\t\t%.2f\t\t\t%.2f\t\t(%.2f, %.2f)\t\t%.2f\n",
                                    elapsedTime, obj.getAcceleration().magnitude(),
                                    obj.getVelo(dt).magnitude(),
                                    obj.getPosition().getX(), obj.getPosition().getY(),
                                    obj.getForce().magnitude() / 1e6));
                            singleWriter.flush();
                        } 
                        if (values.getRelationshipObject1() != null) {
                            VerletObject obj1 = objects.get(0); // Assuming first two objects
                            VerletObject obj2 = objects.get(1);
                            double distance = obj1.getPosition().distance(obj2.getPosition());
                            double gravitationalForce = GravityManager.getMagnitudeForce(obj1, obj2);
                            duoWriter.write(String.format("%.2f\t\t%.2f\t\t%.2f\n", elapsedTime, distance, gravitationalForce / 1e6));
                            duoWriter.flush();
                        } 
                        if (values.isTrackAll()) {
                            allWriter.write(String.format("Time: %.2f\n", elapsedTime));

                            // Chunk size (number of objects per line)
                            int chunkSize = 10;

                            // Write forces in chunks
                            for (int i = 0; i < objects.size(); i++) {
                                if (i % chunkSize == 0) {
                                    if (i > 0) allWriter.write("\n"); // Start a new line for a new chunk
                                    allWriter.write(String.format("Chunk %d:", i / chunkSize + 1));
                                }

                                // Write data for this object
                                VerletObject obj = objects.get(i);
                                allWriter.write(String.format("\tObj%d: %-10.2f", i + 1, obj.getForce().magnitude() / 1e6));
                            }
                            allWriter.write("\n\n");
                            allWriter.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
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
            Point2D Grav = new Point2D(0,1);
            // Update position using Verlet integration
            obj.update(dt);
            
            // Handle boundary conditions
            Point2D pos = obj.getPosition();
            Point2D newPos = new Point2D(
                Math.max(obj.getRadius(), Math.min(WIDTH - obj.getRadius(), pos.getX())),
                Math.max(obj.getRadius(), Math.min(HEIGHT - obj.getRadius(), pos.getY()))
            );
     
            obj.SetPosition(newPos);
            //obj.AddAcceleration(Grav);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}