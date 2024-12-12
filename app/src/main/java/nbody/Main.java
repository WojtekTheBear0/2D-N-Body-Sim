package nbody;

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
import nbody.PhysicsEngine.VerletObject;
import nbody.PhysicsEngine.simulation;
import nbody.gui.Maingui;

import java.util.List;
import java.util.Map;

public class Main extends Application {
    private static final int SIM_WIDTH = 500;    
    private static final int SIM_HEIGHT = 500;   
    
    private simulation sim;
    private long lastTime = 0;
    private int frameCount = 0;
    private long lastFpsTime = 0;
    private Maingui gui;

    private void initializeSimulation() {
        sim = new simulation(SIM_WIDTH, SIM_HEIGHT);
        sim.setSimuationUpdateRate(60);
        sim.setSubStepsCount(5);
        sim.setBoundaries(0, SIM_HEIGHT, 0, SIM_WIDTH);
        // sim.setStreamPosition(0, new Point2D(0, 0));
        // sim.setStreamPosition(1, new Point2D(0, 25));
        // sim.setStreamPosition(2, new Point2D(0, 50));
    }

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Canvas canvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gui = new Maingui();
        initializeSimulation();
        gui.setSimulation(sim);

        // Optionally, set the onRun consumer if needed
        gui.setOnRun(values -> {
            // Implement simulation start logic if required
        });

        Scene scene2 = new Scene(gui.getGrid(), 400, 600);
        Stage secondaryStage = new Stage();
        secondaryStage.setTitle("Simulation Controls");
        secondaryStage.setScene(scene2);

        double offsetX = (screenBounds.getWidth() - SIM_WIDTH) / 2;
        double offsetY = (screenBounds.getHeight() - SIM_HEIGHT) / 2;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frameCount++;
                float dt = (lastTime == 0) ? 0.016f : (float)((now - lastTime) / 1e9);
                lastTime = now;

                // Calculate FPS
                if (now - lastFpsTime >= 1_000_000_000) {
                    // gui.updateFPS(frameCount);
                    frameCount = 0;
                    lastFpsTime = now;
                }

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
                
                gc.setFill(Color.GREY);
                gc.fillRect(offsetX, offsetY, SIM_WIDTH, SIM_HEIGHT);

                if (gui.isRunning()) {
                
                    if (gui.isStreaming()) {
                        sim.AddStream();
                        if(gui.isUsingImageColors()){
                            applyImageColors();
                        }
                    }

             
     
                    sim.update();
                    gui.updateObjectCount();
                }

                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);

                for (VerletObject obj : sim.getSystemManager().getObjects()) {
                    Point2D pos = obj.getPosition();
                    float radius = obj.getRadius();
                    double x = offsetX + pos.getX() - radius;
                    double y = offsetY + pos.getY() - radius;
                    double diameter = radius * 2;
                    
                    gc.setFill(obj.getColor());
                    gc.fillOval(x, y, diameter, diameter);
                    gc.strokeOval(x, y, diameter, diameter);
                }
            }
        };

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        primaryStage.setTitle("N-Body Simulation");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        secondaryStage.show();
        timer.start();
    }

    /**
     * Applies image-based colors to the simulation objects using the last saved color map.
     */
    private void applyImageColors() {
        Map<Integer, Color> streamColorMap = gui.getStreamColorMap();
        List<VerletObject> objects = sim.getSystemManager().getObjects();

        for (int i = 0; i < objects.size(); i++) {
            Color savedColor = streamColorMap.get(i);
            if (savedColor != null) {
                objects.get(i).setColor(savedColor);
                // System.out.println("Applied color to particle " + i);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
