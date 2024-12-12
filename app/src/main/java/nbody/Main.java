package nbody;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nbody.PhysicsEngine.CollisionManager;
import nbody.PhysicsEngine.GravityManager;
import nbody.PhysicsEngine.VerletObject;
import nbody.PhysicsEngine.simulation;
import nbody.gui.CollisionSimulationMenu;
import nbody.gui.Maingui;
import nbody.gui.SimulationTypeMenu;
import nbody.gui.StartMenu;

public class Main extends Application {
    private simulation sim;
    private static final int SIM_WIDTH = 500;    
    private static final int SIM_HEIGHT = 500;
    private Rectangle2D screenBounds;
    private double offsetX;
    private double offsetY;
    private int frameCount = 0;
    private long lastFpsTime = 0;
    private Vector<VerletObject> objects = new Vector<>();
    private CollisionManager collisionManager;
    private long lastTime = 0;
    private AnimationTimer collisionTimer;

    @Override
    public void start(Stage primaryStage) {
        screenBounds = Screen.getPrimary().getVisualBounds();
        offsetX = (screenBounds.getWidth() - SIM_WIDTH) / 2;
        offsetY = (screenBounds.getHeight() - SIM_HEIGHT) / 2;

        StartMenu startMenu = new StartMenu();
        startMenu.show(primaryStage, () -> {
            SimulationTypeMenu simulationTypeMenu = new SimulationTypeMenu();
            simulationTypeMenu.show(primaryStage, 
                () -> runCollisionSimulation(primaryStage),
                () -> runNBodySimulation(primaryStage)
            );
        });
    }

    private void initializeSimulation() {
        sim = new simulation(SIM_WIDTH, SIM_HEIGHT);
        sim.setSimuationUpdateRate(60);
        sim.setSubStepsCount(5);
        sim.setBoundaries(0, SIM_HEIGHT, 0, SIM_WIDTH);
    }

    private void runCollisionSimulation(Stage primaryStage) {
        initializeSimulation();

        // Setup main simulation canvas with correct positioning
        Canvas canvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        // Create and setup GUI window
        Stage controlStage = new Stage();
        CollisionSimulationMenu collisionMenu = new CollisionSimulationMenu();
        collisionMenu.setSimulation(sim);
        Scene controlScene = new Scene(collisionMenu.getGrid(), 300, 500);
        controlStage.setTitle("Simulation Controls");
        controlStage.setScene(controlScene);
        
        // Animation timer for the simulation
        collisionTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Clear entire screen
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
                
                // Draw simulation boundary
                gc.setFill(Color.GREY);
                gc.fillRect(offsetX, offsetY, SIM_WIDTH, SIM_HEIGHT);

                // Only update if simulation is running
                if (collisionMenu.isRunning()) {
                    // Handle streaming if active
                    if (collisionMenu.isStreaming()) {
                        sim.AddStream();
                        if (collisionMenu.isUsingImageColors()) {
                            
                            applyImageColors(collisionMenu);
                        }
                    }
                    
                    // Update simulation
                    sim.update();
                    
                    // Draw all objects with offset
                    for (VerletObject obj : sim.getSystemManager().getObjects()) {
                        Point2D pos = obj.getPosition();
                        float radius = obj.getRadius();
                        gc.setFill(obj.getColor());
                        gc.fillOval(
                            offsetX + pos.getX() - radius, 
                            offsetY + pos.getY() - radius, 
                            radius * 2, 
                            radius * 2
                        );
                    }

                    // Update object count in GUI
                    collisionMenu.updateObjectCount();
                }
            }
        };

        primaryStage.setTitle("Collision Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        controlStage.show();
        collisionTimer.start();

        // Handle window closing
        primaryStage.setOnCloseRequest(e -> {
            if (collisionTimer != null) {
                collisionTimer.stop();
            }
            controlStage.close();
        });

        controlStage.setOnCloseRequest(e -> {
            if (collisionTimer != null) {
                collisionTimer.stop();
            }
            primaryStage.close();
        });
    }

    
    
    private void runNBodySimulation(Stage primaryStage) {
        Maingui gui = new Maingui();
        gui.setOnRun(values -> setupNBodySimulation(primaryStage, gui, values));

        Scene guiScene = new Scene(gui.getGrid(), 300, 475);
        primaryStage.setTitle("N-Body Configuration");
        primaryStage.setScene(guiScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setupNBodySimulation(Stage primaryStage, Maingui gui, Maingui.SimulationValues values) {
        Canvas canvas = new Canvas(SIM_WIDTH, SIM_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        initializeObjects(values.getObjectCount(), values.getMass(), values.getMassVariance(),
                values.getDiameter(), values.getDiameterVariance());
        collisionManager = new CollisionManager(objects);

        setupNBodyAnimationTimer(gc, values, primaryStage);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, SIM_WIDTH, SIM_HEIGHT);

        primaryStage.setTitle("N-Body Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        gui.close();
    }

    private void setupNBodyAnimationTimer(GraphicsContext gc, Maingui.SimulationValues values, Stage primaryStage) {
        AnimationTimer timer = new AnimationTimer() {
            private double elapsedTime = 0;
            private BufferedWriter singleWriter;

            @Override
            public void handle(long now) {
                float dt = (lastTime == 0) ? 0.016f : (float)((now - lastTime) / 1e9);
                lastTime = now;
                elapsedTime += dt;

                if (elapsedTime > values.getTimeToTrack()) {
                    stop();
                    return;
                }

                updateSimulation(gc, dt, values);
            }

            @Override
            public void stop() {
                try {
                    if (singleWriter != null) singleWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                super.stop();
            }
        };
        timer.start();
    }

    private void updateSimulation(GraphicsContext gc, float dt, Maingui.SimulationValues values) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, SIM_WIDTH, SIM_HEIGHT);

        updateObjects(dt);

        if (values.useBruteForce()) {
            collisionManager.BruteForceSolve();
            GravityManager.computeForcesBruteForce(objects);
        } else {
            collisionManager.quadTreeCollision();
            GravityManager.computeForcesWithQuadTree(objects);
        }

        // Draw objects
        gc.setFill(Color.SKYBLUE);
        for (VerletObject obj : objects) {
            Point2D pos = obj.getPosition();
            float radius = obj.getRadius();
            gc.fillOval(pos.getX() - radius, pos.getY() - radius, radius * 2, radius * 2);
        }
    }

    private void initializeObjects(int numBodies, double mass, double massVariance, double diameter, double diameterVariance) {
        Random rand = new Random();
        objects.clear();
        for (int i = 0; i < numBodies; i++) {
            double x = rand.nextDouble() * SIM_WIDTH;
            double y = rand.nextDouble() * SIM_HEIGHT;
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
                Math.max(obj.getRadius(), Math.min(SIM_WIDTH - obj.getRadius(), pos.getX())),
                Math.max(obj.getRadius(), Math.min(SIM_HEIGHT - obj.getRadius(), pos.getY()))
            );
            obj.SetPosition(newPos);
        }
    }

    private void applyImageColors(CollisionSimulationMenu collisionMenu) {
        Map<Integer, Color> streamColorMap = collisionMenu.getStreamColorMap();
        List<VerletObject> objects = sim.getSystemManager().getObjects();
        
        for (int i = 0; i < objects.size(); i++) {
            Color savedColor = streamColorMap.get(i);
            if (savedColor != null) {
                objects.get(i).setColor(savedColor);
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}