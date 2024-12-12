package nbody.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nbody.PhysicsEngine.CollisionSystem; // Create this enum if not existing
import nbody.PhysicsEngine.ImageQualityHandler;
import nbody.PhysicsEngine.VerletObject;
import nbody.PhysicsEngine.simulation;





public class CollisionSimulationMenu extends Application {
    private static final int SIM_WIDTH = 500;
    private static final int SIM_HEIGHT = 500;

    private GridPane grid;
    private Consumer<SimulationValues> onRun;
    private Button runButton;
    private Button loadImageButton;
    private Button clearButton;
    private Button toggleRunButton;
    private RadioButton bruteForceButton;
    private RadioButton quadTreeButton;
    private RadioButton spatialHashButton;
    private ToggleGroup collisionGroup;
    private Button addObjectButton;
    private Button toggleStreamButton;
    private Slider streamRateSlider;
    private Slider streamCountSlider;
    private Label objectCountLabel;
    private CheckBox useImageColorsCheckBox; // Added CheckBox
    private boolean isStreaming = false;
    private boolean isRunning = true;
    private simulation sim;

    private boolean useImageColors = false;
    private Map<Integer, Color> streamColorMap = new HashMap<>();
    private int totalParticlesCreated = 0;
    private ImageQualityHandler imageHandler = new ImageQualityHandler();

    public CollisionSimulationMenu() {
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        initializeButtons();
        setupButtonHandlers();
        addButtonsToGrid();
    }

    private void initializeButtons() {
        runButton = new Button("Run Simulation");
        loadImageButton = new Button("Load Image Colors");
        clearButton = new Button("Clear Scene");
        toggleRunButton = new Button("Pause");

        collisionGroup = new ToggleGroup();

        bruteForceButton = new RadioButton("Brute Force");
        quadTreeButton = new RadioButton("Quad Tree");
        spatialHashButton = new RadioButton("Spatial Hash");

        bruteForceButton.setToggleGroup(collisionGroup);
        quadTreeButton.setToggleGroup(collisionGroup);
        spatialHashButton.setToggleGroup(collisionGroup);
        bruteForceButton.setSelected(true);

        addObjectButton = new Button("Add Single Object");
        toggleStreamButton = new Button("Start Stream");

        streamRateSlider = new Slider(1, 60, 10);
        streamRateSlider.setShowTickLabels(true);
        streamRateSlider.setShowTickMarks(true);
        streamRateSlider.setMajorTickUnit(10);
        streamRateSlider.setBlockIncrement(1);
        streamRateSlider.setTooltip(new Tooltip("Objects per second"));

        streamCountSlider = new Slider(1, 10, 3);
        streamCountSlider.setShowTickLabels(true);
        streamCountSlider.setShowTickMarks(true);
        streamCountSlider.setMajorTickUnit(1);
        streamCountSlider.setBlockIncrement(1);
        streamCountSlider.setMinorTickCount(0);
        streamCountSlider.setSnapToTicks(true);
        streamCountSlider.setTooltip(new Tooltip("Objects per stream"));

        objectCountLabel = new Label("Objects: 0");

        // Initialize the Use Image Colors CheckBox
        useImageColorsCheckBox = new CheckBox("Use Image Colors");
        useImageColorsCheckBox.setSelected(useImageColors); // Set initial state
    }

    private void setupButtonHandlers() {
        runButton.setOnAction(e -> {
            isRunning = true;
            isStreaming = true;
            if (onRun != null) {
                SimulationValues values = new SimulationValues(
                    10.0, 1.0, 10.0, 1.0, 50, 10.0,
                    null, null, null, false, true
                );
                onRun.accept(values);
            }
        });

        loadImageButton.setOnAction(e -> {
            if (sim != null) {
                loadImageColors();
            }
        });

        clearButton.setOnAction(e -> {
            if (sim != null) {
                sim.getSystemManager().clearObjects();
                updateObjectCount();
                resetColorMap(); // Reset color mapping after clearing
            }
        });

        toggleRunButton.setOnAction(e -> {
            isRunning = !isRunning;
            toggleRunButton.setText(isRunning ? "Pause" : "Run");
        });

        bruteForceButton.setOnAction(e -> {
            if (sim != null) sim.setCollisionSystem(CollisionSystem.BRUTE_FORCE);
        });

        quadTreeButton.setOnAction(e -> {
            if (sim != null) sim.setCollisionSystem(CollisionSystem.QUAD_TREE);
        });

        spatialHashButton.setOnAction(e -> {
            if (sim != null) sim.setCollisionSystem(CollisionSystem.SPATIAL_HASH);
        });

        addObjectButton.setOnAction(e -> {
            if (sim != null) {
                Point2D center = new Point2D(SIM_WIDTH / 2, SIM_HEIGHT / 2);
                // sim.addObjectInstantly(center);
                updateObjectCount();
            }
        });

        toggleStreamButton.setOnAction(e -> {
            if (sim != null) {
                isStreaming = !isStreaming;
                toggleStreamButton.setText(isStreaming ? "Stop Stream" : "Start Stream");
                
                // Only set parameters if streaming is being started
                if (isStreaming) {
                    sim.setStreamActive(true);
                    sim.setStreamRate(streamRateSlider.getValue());
                    sim.setObjectsPerStream((int) streamCountSlider.getValue());
                } else {
                    sim.setStreamActive(false);
                }
            }
        });
        
        streamRateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sim != null && isStreaming) {
                sim.setStreamRate(newVal.doubleValue());
            }
        });

        streamCountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sim != null && isStreaming) {
                int objectsPerStream = newVal.intValue();
                sim.setObjectsPerStream(objectsPerStream);
            }
        });

        // Handle Use Image Colors CheckBox
        useImageColorsCheckBox.setOnAction(e -> {
            useImageColors = useImageColorsCheckBox.isSelected();
            if (sim != null) {
                if (useImageColors) {
                    applyExistingColorMap();
                } else {
                    resetObjectColors();
                }
            }
        });

        Button closeButton = new Button("Close Simulation");
    closeButton.setOnAction(e -> {
        // Gracefully shut down the simulation
        if (sim != null) {
            // Stop spawning
            sim.pauseSpawning();
            
            // Clear objects
            Platform.runLater(() -> {
                sim.clearObjects();
                
                // Close any open stages or windows
                Stage stage = (Stage) grid.getScene().getWindow();
                stage.close();
            });
        }
    });


    grid.add(closeButton, 0, 6); // Adjust the row as needed

    }

    private void addButtonsToGrid() {
        VBox collisionBox = new VBox(5);
        collisionBox.getChildren().addAll(
            new Label("Collision System:"),
            bruteForceButton,
            quadTreeButton,
            spatialHashButton
        );

        VBox objectBox = new VBox(5);
        objectBox.getChildren().addAll(
            new Label("Objects:"),
            addObjectButton,
            new Label("Stream Controls:"),
            new Label("Rate (per second):"),
            streamRateSlider,
            new Label("Count per stream:"),
            streamCountSlider,
            toggleStreamButton,
            objectCountLabel,
            useImageColorsCheckBox // Add the CheckBox here
        );

        grid.add(collisionBox, 0, 0);
        grid.add(objectBox, 0, 1);
        grid.add(runButton, 0, 2);
        grid.add(loadImageButton, 0, 3);
        grid.add(clearButton, 0, 4);
        grid.add(toggleRunButton, 0, 5);
    }


    private Color getColorForPosition(Point2D position) {
        return imageHandler.getColorForPosition(position, SIM_WIDTH, SIM_HEIGHT);
    }

    

    /**
     * Loads image colors and populates the streamColorMap.
     */
    private void loadImageColors() {
        try {
            Image sourceImage = new Image(getClass().getResourceAsStream("/images/JOIN.png"));
            if (sourceImage.isError()) {
                System.err.println("Error loading image: " + sourceImage.getException());
                return;
            }

            imageHandler.loadAndProcessImage(sourceImage, SIM_WIDTH, SIM_HEIGHT);
            useImageColors = true;

            streamColorMap.clear();
            List<VerletObject> objects = sim.getSystemManager().getObjects();
            for (int i = 0; i < objects.size(); i++) {
                Point2D pos = objects.get(i).getPosition();
                Color color = getColorForPosition(pos);
                streamColorMap.put(i, color);
                objects.get(i).setColor(color);
                // System.out.println("Saved color for particle " + i + ": " + color);
            }
            totalParticlesCreated = objects.size();

            // Optionally, automatically apply the color map if "Use Image Colors" is checked
            if (useImageColors) {
                applyExistingColorMap();
            }

        } catch (Exception e) {
            System.err.println("Failed to load image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Applies the existing color map to the simulation objects.
     * This method ensures that the color map is applied without reloading the image.
     */
    private void applyExistingColorMap() {
        if (streamColorMap.isEmpty()) {
            System.err.println("No color map loaded. Please load image colors first.");
            useImageColorsCheckBox.setSelected(false);
            useImageColors = false;
            return;
        }

        List<VerletObject> objects = sim.getSystemManager().getObjects();

        for (int i = 0; i < objects.size(); i++) {
            Color savedColor = streamColorMap.get(i);
            if (savedColor != null) {
                objects.get(i).setColor(savedColor);
                System.out.println("Applied color to particle " + i);
            }
        }
    }

    /**
     * Resets the color map after clearing the simulation.
     */
    private void resetColorMap() {
        // Optionally reset the color map or maintain it for future runs
        // Here, we maintain the color map for future simulation runs
        // If you want to reset it, uncomment the following line:
        // streamColorMap.clear();
        
    }

    private void resetObjectColors() {
        useImageColors = false;
        // Keep the streamColorMap intact for future runs
        List<VerletObject> objects = sim.getSystemManager().getObjects();
        for (VerletObject obj : objects) {
            obj.setColor(Color.WHITE); // Reset to default color
        }
    }

    public void updateObjectCount() {
        if (sim != null) {
            int count = sim.getSystemManager().getObjects().size();
            objectCountLabel.setText("Objects: " + count);
        }
    }

    public void setSimulation(simulation sim) {
        this.sim = sim;
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(grid, 300, 500); // Increased height to accommodate new CheckBox
        primaryStage.setTitle("N-Body Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void setOnRun(Consumer<SimulationValues> onRun) {
        this.onRun = onRun;
    }

    public void close() {
        ((Stage) grid.getScene().getWindow()).close();
    }

    public GridPane getGrid() {
        return grid;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isUsingImageColors() {
        return useImageColors;
    }

    public Map<Integer, Color> getStreamColorMap() {
        return streamColorMap;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class SimulationValues {
        private final double mass;
        private final double massVariance;
        private final double diameter;
        private final double diameterVariance;
        private final int objectCount;
        private final double timeToTrack;
        private final Integer singleTrackObject;
        private final Integer relationshipObject1;
        private final Integer relationshipObject2;
        private final boolean trackAll;
        private final boolean bruteForce;

        public SimulationValues(double mass, double massVariance, double diameter, double diameterVariance,
                                int objectCount, double timeToTrack, Integer singleTrackObject,
                                Integer relationshipObject1, Integer relationshipObject2, boolean trackAll,
                                boolean bruteForce) {
            this.mass = mass;
            this.massVariance = massVariance;
            this.diameter = diameter;
            this.diameterVariance = diameterVariance;
            this.objectCount = objectCount;
            this.timeToTrack = timeToTrack;
            this.singleTrackObject = singleTrackObject;
            this.relationshipObject1 = relationshipObject1;
            this.relationshipObject2 = relationshipObject2;
            this.trackAll = trackAll;
            this.bruteForce = bruteForce;
        }

        public double getMass() { return mass; }
        public double getMassVariance() { return massVariance; }
        public double getDiameter() { return diameter; }
        public double getDiameterVariance() { return diameterVariance; }
        public int getObjectCount() { return objectCount; }
        public double getTimeToTrack() { return timeToTrack; }
        public Integer getSingleTrackObject() { return singleTrackObject; }
        public Integer getRelationshipObject1() { return relationshipObject1; }
        public Integer getRelationshipObject2() { return relationshipObject2; }
        public boolean isTrackAll() { return trackAll; }
        public boolean useBruteForce() { return bruteForce; }
    }
}