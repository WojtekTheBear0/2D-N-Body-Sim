import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class BodySimGUI extends Application 
{
   public static void main(String[] args) 
   {
      launch(args);
   }


   public void start(Stage primaryStage)
   {
      Pattern pattern = Pattern.compile("\\d*\\.?\\d*");

      UnaryOperator<TextFormatter.Change> filter = change -> {
            if (pattern.matcher(change.getControlNewText()).matches()) {
                return change;
            }
            return null;
        };

      primaryStage.setTitle("N-Body Simulation Setup");

      // Grid layout for arranging controls
      GridPane grid = new GridPane();
      grid.setPadding(new Insets(10, 10, 10, 10));
      grid.setVgap(8);
      grid.setHgap(10);

      // Create and add controls
      Label massLabel = new Label("Mass (kg):");
      TextField massInput = new TextField();
      massInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(massLabel, 0, 0);
      grid.add(massInput, 1, 0);

      Label massVarianceLabel = new Label("Mass Variance:");
      TextField massVarianceInput = new TextField();
      massVarianceInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(massVarianceLabel, 0, 1);
      grid.add(massVarianceInput, 1, 1);

      Label accelerationLabel = new Label("Acceleration (m/s²):");
      TextField accelerationInput = new TextField();
      accelerationInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(accelerationLabel, 0, 2);
      grid.add(accelerationInput, 1, 2);

      Label accVarianceLabel = new Label("Acceleration Variance:");
      TextField accVarianceInput = new TextField();
      accVarianceInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(accVarianceLabel, 0, 3);
      grid.add(accVarianceInput, 1, 3);

      Label distanceLabel = new Label("Distance (m):");
      TextField distanceInput = new TextField();
      distanceInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(distanceLabel, 0, 4);
      grid.add(distanceInput, 1, 4);

      Label distanceVarianceLabel = new Label("Distance Variance:");
      TextField distanceVarianceInput = new TextField();
      distanceVarianceInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(distanceVarianceLabel, 0, 5);
      grid.add(distanceVarianceInput, 1, 5);

      Label objectCountLabel = new Label("Number of Objects:");
      TextField objectCountInput = new TextField();
      objectCountInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(objectCountLabel, 0, 6);
      grid.add(objectCountInput, 1, 6);

      Label timestepLabel = new Label("Integration Timesteps:");
      TextField timestepInput = new TextField();
      timestepInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(timestepLabel, 0, 7);
      grid.add(timestepInput, 1, 7);

      CheckBox trackParticlesCheck = new CheckBox("Track Specific Object");
      grid.add(trackParticlesCheck, 0, 8, 2, 1);

      Label singleTrackLabel = new Label("Particle to Track:");
      TextField singleTrackInput = new TextField();
      singleTrackInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(singleTrackLabel, 0, 9);
      grid.add(singleTrackInput, 1, 9);

      CheckBox trackRelationshipsCheck = new CheckBox("Track Specific Relationship");
      grid.add(trackRelationshipsCheck, 0, 10, 2, 1);

      Label firstObjectLabel = new Label("Particle 1:");
      TextField firstObjectInput = new TextField();
      firstObjectInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(firstObjectLabel, 0, 11);
      grid.add(firstObjectInput, 1, 11);

      Label secondObjectLabel = new Label("Particle 2:");
      TextField secondObjectInput = new TextField();
      secondObjectInput.setTextFormatter(new TextFormatter<>(filter));
      grid.add(secondObjectLabel, 0, 12);
      grid.add(secondObjectInput, 1, 12);

      Button runButton = new Button("Run Simulation");
      grid.add(runButton, 1, 13);

      // Event handler for the run button
      runButton.setOnAction(e -> openVisualizationWindow());

      Scene scene = new Scene(grid, 400, 500);
      primaryStage.setScene(scene);
      primaryStage.show();
   }


   private void openVisualizationWindow() 
   {
      Stage visualizationStage = new Stage();
      visualizationStage.setTitle("Simulation Visualization");

      // Set up the scene for visualization
      Label label = new Label("Visualization window - simulation will run here.");
      Scene scene = new Scene(label, 600, 600);
      visualizationStage.setScene(scene);
      visualizationStage.show();
  }
}