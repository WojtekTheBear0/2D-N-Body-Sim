package nbody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nbody.PhysicsEngine.ImageQualityHandler;
import nbody.PhysicsEngine.VerletObject;
import nbody.PhysicsEngine.simulation;
import nbody.gui.Maingui;

public class Main extends Application {
    private static final int SIM_WIDTH = 500;    
    private static final int SIM_HEIGHT = 500;   
    
    private simulation sim;
    private long lastTime = 0;
    private boolean useImageColors = false;
    private Map<Integer, Color> streamColorMap = new HashMap<>();
    private int colorIndex = 0;
    private boolean isRunning = true;
    private int totalParticlesCreated = 0;
    
    private ImageQualityHandler imageHandler = new ImageQualityHandler();

    private void loadImageColors() {
        try {
            Image sourceImage = new Image(getClass().getResourceAsStream("/images/lancer.png"));
            if (sourceImage.isError()) {
                System.err.println("Error loading image: " + sourceImage.getException());
                return;
            }
            
            imageHandler.loadAndProcessImage(sourceImage, SIM_WIDTH, SIM_HEIGHT);
            useImageColors = true;
            
            // Save current particle positions and their colors
            streamColorMap.clear();
            List<VerletObject> objects = sim.getSystemManager().getObjects();
            for (int i = 0; i < objects.size(); i++) {
                Point2D pos = objects.get(i).getPosition();
                Color color = getColorForPosition(pos);
                streamColorMap.put(i, color);
                objects.get(i).setColor(color);  // Immediately apply color
                System.out.println("Saved color for particle " + i + ": " + color); // Debug print
            }
            totalParticlesCreated = objects.size();
            colorIndex = objects.size();  // Set color index to current size
            
        } catch (Exception e) {
            System.err.println("Failed to load image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Color getColorForPosition(Point2D position) {
        return imageHandler.getColorForPosition(position, SIM_WIDTH, SIM_HEIGHT);
    }

    private void initializeSimulation() {
        sim = new simulation(SIM_WIDTH, SIM_HEIGHT);
        sim.setSimuationUpdateRate(60);
        sim.setSubStepsCount(5);
        sim.setBoundaries(0, SIM_HEIGHT, 0, SIM_WIDTH);

        sim.setStreamPosition(0, new Point2D(0, 0));
        sim.setStreamPosition(1, new Point2D(0, 25));
        sim.setStreamPosition(2, new Point2D(0, 50));
        
        colorIndex = 0;
    }

    private void clearScene() {
        sim.getSystemManager().clearObjects();
    }

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        Canvas canvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        Maingui gui = new Maingui();
        
        Button loadImageButton = new Button("Load Image Colors");
        loadImageButton.setOnAction(e -> {
            loadImageColors();
            colorIndex = 0;
        });
        
        Button clearButton = new Button("Clear Scene");
        clearButton.setOnAction(e -> {
            clearScene();
            colorIndex = 0;  // Reset color index but keep the color mapping
        });
        
        Button toggleRunButton = new Button("Pause");
        toggleRunButton.setOnAction(e -> {
            isRunning = !isRunning;
            toggleRunButton.setText(isRunning ? "Pause" : "Run");
        });
        
        gui.getGrid().add(loadImageButton, 0, gui.getGrid().getRowCount());
        gui.getGrid().add(clearButton, 0, gui.getGrid().getRowCount());
        gui.getGrid().add(toggleRunButton, 0, gui.getGrid().getRowCount());
        
        Scene scene2 = new Scene(gui.getGrid(), 400, 600);
        Stage secondaryStage = new Stage();
        secondaryStage.setTitle("Simulation Controls");
        secondaryStage.setScene(scene2);

        initializeSimulation();

        double offsetX = (screenBounds.getWidth() - SIM_WIDTH) / 2;
        double offsetY = (screenBounds.getHeight() - SIM_HEIGHT) / 2;

        AnimationTimer timer = new AnimationTimer() {
            private int frameCount = 0;
            
            @Override
            public void handle(long now) {
                float dt = (lastTime == 0) ? 0.016f : (float)((now - lastTime) / 1e9);
                lastTime = now;

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
                
                gc.setFill(Color.GREY);
                gc.fillRect(offsetX, offsetY, SIM_WIDTH, SIM_HEIGHT);

                if (isRunning) {
                    sim.AddStream();
                    if (useImageColors) {
                        List<VerletObject> objects = sim.getSystemManager().getObjects();
                        while (colorIndex < objects.size() && colorIndex < streamColorMap.size()) {
                            Color savedColor = streamColorMap.get(colorIndex);
                            if (savedColor != null) {
                                objects.get(colorIndex).setColor(savedColor);
                                System.out.println("Applied color to new particle " + colorIndex); // Debug print
                            }
                            colorIndex++;
                        }
                    }
                    sim.update();
                    frameCount++;
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
        primaryStage.setTitle("N-Body Simulation with Verlet Integration");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        secondaryStage.show();
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}