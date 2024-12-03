package nbody.gui;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;


import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Maingui {

    private GridPane grid;

    // GUI elements
    private Label massLabel;
    private TextField massField;

    private Label massVarianceLabel;
    private TextField massVarianceField;

    private Label accelerationLabel;
    private TextField accelerationField;

    private Label accVarianceLabel;
    private TextField accVarianceField;

    private Label distanceLabel;
    private TextField distanceField;

    private Label distanceVarianceLabel;
    private TextField distanceVarianceField;

    private Label objectCountLabel;
    private TextField objectCountField;

    private Label timestepLabel;
    private TextField timestepField;

    private Label singleTrackLabel;
    private TextField singleTrackField;

    private CheckBox trackRelationshipsCheck;
    private Label firstObjectLabel;
    private TextField firstObjectField;

    private Label secondObjectLabel;
    private TextField secondObjectField;

    private Button runButton;

    public Maingui() {
        // Initialize the grid
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        // Initialize components
        massLabel = new Label("Mass (kg):");
        massField = createNumericTextField();

        massVarianceLabel = new Label("Mass Variance:");
        massVarianceField = createNumericTextField();

        accelerationLabel = new Label("Acceleration (m/s²):");
        accelerationField = createNumericTextField();

        accVarianceLabel = new Label("Acceleration Variance:");
        accVarianceField = createNumericTextField();

        distanceLabel = new Label("Distance (m):");
        distanceField = createNumericTextField();

        distanceVarianceLabel = new Label("Distance Variance:");
        distanceVarianceField = createNumericTextField();

        objectCountLabel = new Label("Number of Objects:");
        objectCountField = createNumericTextField();

        timestepLabel = new Label("Integration Timesteps:");
        timestepField = createNumericTextField();

        singleTrackLabel = new Label("Particle to Track:");
        singleTrackField = createNumericTextField();

        trackRelationshipsCheck = new CheckBox("Track Specific Relationship");

        firstObjectLabel = new Label("Particle 1:");
        firstObjectField = createNumericTextField();

        secondObjectLabel = new Label("Particle 2:");
        secondObjectField = createNumericTextField();

        runButton = new Button("Run Simulation");

        // Add components to the grid
        int row = 0;
        grid.add(massLabel, 0, row);
        grid.add(massField, 1, row++);

        grid.add(massVarianceLabel, 0, row);
        grid.add(massVarianceField, 1, row++);

        grid.add(accelerationLabel, 0, row);
        grid.add(accelerationField, 1, row++);

        grid.add(accVarianceLabel, 0, row);
        grid.add(accVarianceField, 1, row++);

        grid.add(distanceLabel, 0, row);
        grid.add(distanceField, 1, row++);

        grid.add(distanceVarianceLabel, 0, row);
        grid.add(distanceVarianceField, 1, row++);

        grid.add(objectCountLabel, 0, row);
        grid.add(objectCountField, 1, row++);

        grid.add(timestepLabel, 0, row);
        grid.add(timestepField, 1, row++);

        grid.add(singleTrackLabel, 0, row);
        grid.add(singleTrackField, 1, row++);

        grid.add(trackRelationshipsCheck, 0, row, 2, 1);
        row++;

        grid.add(firstObjectLabel, 0, row);
        grid.add(firstObjectField, 1, row++);

        grid.add(secondObjectLabel, 0, row);
        grid.add(secondObjectField, 1, row++);

        grid.add(runButton, 0, row, 2, 1);
    }

    // Method to create numeric TextField
    private TextField createNumericTextField() {
        TextField textField = new TextField();
        Pattern pattern = Pattern.compile("\\d*"); // Only digits allowed
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
}
