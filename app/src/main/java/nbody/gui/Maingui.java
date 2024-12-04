package nbody.gui;

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

    // GUI elements
    private Label massLabel;
    private TextField massField;

    private Label massVarianceLabel;
    private TextField massVarianceField;

    private Label distanceLabel;
    private TextField distanceField;

    private Label distanceVarianceLabel;
    private TextField distanceVarianceField;

    private Label objectCountLabel;
    private TextField objectCountField;

    private Label timestepLabel;
    private TextField timestepField;

    private CheckBox trackSingleCheck;
    private Label singleTrackLabel;
    private TextField singleTrackField;

    private CheckBox trackRelationshipsCheck;
    private Label firstObjectLabel;
    private TextField firstObjectField;

    private Label secondObjectLabel;
    private TextField secondObjectField;

    private CheckBox allCheck;

    private Button backButton;
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

        distanceLabel = new Label("Distance (m):");
        distanceField = createNumericTextField();

        distanceVarianceLabel = new Label("Distance Variance:");
        distanceVarianceField = createNumericTextField();

        objectCountLabel = new Label("Number of Objects:");
        objectCountField = createNumericTextField();

        timestepLabel = new Label("Integration Timesteps:");
        timestepField = createNumericTextField();

        trackSingleCheck = new CheckBox("Track Object");

        singleTrackLabel = new Label("Object to Track:");
        singleTrackField = createNumericTextField();

        trackRelationshipsCheck = new CheckBox("Track Specific Relationship");

        firstObjectLabel = new Label("Object 1:");
        firstObjectField = createNumericTextField();

        secondObjectLabel = new Label("Object 2:");
        secondObjectField = createNumericTextField();

        allCheck = new CheckBox("Track All Objects");

        backButton = new Button("Cancel");
        runButton = new Button("Run Simulation");

        // Add components to the grid
        int row = 0;
        grid.add(massLabel, 0, row);
        grid.add(massField, 1, row++);

        grid.add(massVarianceLabel, 0, row);
        grid.add(massVarianceField, 1, row++);

        grid.add(distanceLabel, 0, row);
        grid.add(distanceField, 1, row++);

        grid.add(distanceVarianceLabel, 0, row);
        grid.add(distanceVarianceField, 1, row++);

        grid.add(objectCountLabel, 0, row);
        grid.add(objectCountField, 1, row++);

        grid.add(timestepLabel, 0, row);
        grid.add(timestepField, 1, row++);

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

        grid.add(backButton, 0, row, 2, 1);
        grid.add(runButton, 1, row, 2, 1);
    }


    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(getGrid(), 400, 400);
        primaryStage.setTitle("N-Body Simulation");
        primaryStage.setScene(scene);

        primaryStage.show();
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
}
