package nbody.gui;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Maingui extends Application {

    private GridPane grid;
    private Consumer<SimulationValues> onRun;

    // GUI elements
    private Label massLabel;
    private TextField massField;

    private Label massVarianceLabel;
    private TextField massVarianceField;

    private Label diameterLabel;
    private TextField diameterField;

    private Label diameterVarianceLabel;
    private TextField diameterVarianceField;

    private Label objectCountLabel;
    private TextField objectCountField;

    private Label trackDurationLabel;
    private TextField trackDurationField;

    private CheckBox trackSingleCheck;
    private Label singleTrackLabel;
    private TextField singleTrackField;

    private CheckBox trackRelationshipsCheck;
    private Label firstObjectLabel;
    private TextField firstObjectField;

    private Label secondObjectLabel;
    private TextField secondObjectField;

    private CheckBox allCheck;

    private CheckBox bruteForceCheck;

    private Button runButton;

    public Maingui() {
        // Initialize the grid
        grid = new GridPane();
        
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        // Initialize components
        massLabel = new Label("Mass * 10^12 (kg):");
        massField = createNumericTextField();

        massVarianceLabel = new Label("Mass Variance:");
        massVarianceField = createNumericTextField();

        diameterLabel = new Label("Diameter (m):");
        diameterField = createNumericTextField();

        diameterVarianceLabel = new Label("Diameter Variance:");
        diameterVarianceField = createNumericTextField();

        objectCountLabel = new Label("Number of Objects:");
        objectCountField = createNumericTextField();

        trackDurationLabel = new Label("Time to Track (s):");
        trackDurationField = createNumericTextField();

        trackSingleCheck = new CheckBox("Track Object");

        singleTrackLabel = new Label("Object to Track:");
        singleTrackField = createNumericTextField();

        trackRelationshipsCheck = new CheckBox("Track Specific Relationship");

        firstObjectLabel = new Label("Object 1:");
        firstObjectField = createNumericTextField();

        secondObjectLabel = new Label("Object 2:");
        secondObjectField = createNumericTextField();

        allCheck = new CheckBox("Track All Objects");

        bruteForceCheck = new CheckBox("Use Brute Force Calculation");
        bruteForceCheck.setSelected(true);

        runButton = new Button("Run Simulation");


        // Update checkboxes to enable/disable relevant fields
        trackSingleCheck.setOnAction(e -> {
            boolean selected = trackSingleCheck.isSelected();
            singleTrackLabel.setDisable(!selected);
            singleTrackField.setDisable(!selected);
        });

        trackRelationshipsCheck.setOnAction(e -> {
            boolean selected = trackRelationshipsCheck.isSelected();
            firstObjectLabel.setDisable(!selected);
            firstObjectField.setDisable(!selected);
            secondObjectLabel.setDisable(!selected);
            secondObjectField.setDisable(!selected);
        });


        runButton.setOnAction(e -> {
            if (onRun != null) {
                Integer singleTrackObject = trackSingleCheck.isSelected() ? Integer.parseInt(singleTrackField.getText()) : null;
                Integer relationshipObject1 = trackRelationshipsCheck.isSelected() ? Integer.parseInt(firstObjectField.getText()) : null;
                Integer relationshipObject2 = trackRelationshipsCheck.isSelected() ? Integer.parseInt(secondObjectField.getText()) : null;
        
                SimulationValues values = new SimulationValues(
                        Double.parseDouble(massField.getText()),
                        Double.parseDouble(massVarianceField.getText()),
                        Double.parseDouble(diameterField.getText()),
                        Double.parseDouble(diameterVarianceField.getText()),
                        Integer.parseInt(objectCountField.getText()),
                        Double.parseDouble(trackDurationField.getText()),
                        singleTrackObject,
                        relationshipObject1,
                        relationshipObject2,
                        allCheck.isSelected(),
                        bruteForceCheck.isSelected()
                );
                onRun.accept(values);
            }
        });

        // Add components to the grid
        int row = 0;
        grid.add(massLabel, 0, row);
        grid.add(massField, 1, row++);

        grid.add(massVarianceLabel, 0, row);
        grid.add(massVarianceField, 1, row++);

        grid.add(diameterLabel, 0, row);
        grid.add(diameterField, 1, row++);

        grid.add(diameterVarianceLabel, 0, row);
        grid.add(diameterVarianceField, 1, row++);

        grid.add(objectCountLabel, 0, row);
        grid.add(objectCountField, 1, row++);

        grid.add(trackDurationLabel, 0, row);
        grid.add(trackDurationField, 1, row++);

        grid.add(trackSingleCheck, 0, row, 2, 1);
        row++;

        grid.add(singleTrackLabel, 0, row);
        grid.add(singleTrackField, 1, row++);

        grid.add(trackRelationshipsCheck, 0, row, 2, 1);
        row++;

        grid.add(firstObjectLabel, 0, row);
        grid.add(firstObjectField, 1, row++);

        grid.add(secondObjectLabel, 0, row);
        grid.add(secondObjectField, 1, row++);

        grid.add(allCheck, 0, row, 2, 1);
        row++;

        grid.add(bruteForceCheck, 0, row, 2, 1);
        row++;

        grid.add(runButton, 1, row, 1, 1);
    }


    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(getGrid(), 300, 450);
        primaryStage.setTitle("N-Body Simulation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    
    public void setOnRun(Consumer<SimulationValues> onRun) {
        this.onRun = onRun;
    }


    public void close() {
        ((Stage) grid.getScene().getWindow()).close();
    }


    // Method to create numeric TextField
    private TextField createNumericTextField() {
        TextField textField = new TextField();
        Pattern pattern = Pattern.compile("\\d*\\.?\\d*"); // Only digits allowed
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (pattern.matcher(change.getControlNewText()).matches()) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);
        return textField;
    }


    // Getter for the grid
    public GridPane getGrid() {
        return grid;
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

        public double getMass() {
            return mass;
        }

        public double getMassVariance() {
            return massVariance;
        }

        public double getDiameter() {
            return diameter;
        }

        public double getDiameterVariance() {
            return diameterVariance;
        }

        public int getObjectCount() {
            return objectCount;
        }

        public double getTimeToTrack() {
            return timeToTrack;
        }

        public Integer getSingleTrackObject() {
            return singleTrackObject;
        }

        public Integer getRelationshipObject1() {
            return relationshipObject1;
        }

        public Integer getRelationshipObject2() {
            return relationshipObject2;
        }

        public boolean isTrackAll() {
            return trackAll;
        }

        public boolean useBruteForce() {
            return bruteForce;
        }
    }
}
