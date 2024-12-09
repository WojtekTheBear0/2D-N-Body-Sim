package nbody.PhysicsEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public class SpatialHashing<T> {
    // Constants
    private static final int MAX_OBJECTS_PER_CELL = 100;
    private static final int MAX_NEIGHBORS = 8;
    
    // Configuration
    private final float min = 0;
    private final float max = 500;
    private final int cellSize = 25;
    private final int width;
    private final int conversionFactor;
    
    // Main storage
    private final Map<Integer, Set<T>> grid;
    
    // Reusable collections to prevent allocations
    private final Set<T> reusableSet;
    private final List<T> reusableList;
    private final GridCell tempCell;
    private final Point2D tempPoint;
    private final GridCell[] neighborCells;
    
    public interface CollisionCallback<T> {
        void processCollision(T object1, T object2);
    }
    
    private static class GridCell {
        int gridCell;
        
        // Im trying to avoid division
        void update(Point2D point, int cellSize, int conversionFactor, int width) {
            int x = (int)(point.getX() / cellSize);
            int y = (int)(point.getY() / cellSize);
            this.gridCell = x * conversionFactor + y * conversionFactor * width;
        }
        
    //Ignore this im dumb.
        void update(int x, int y, int width) {
            this.gridCell = x + y * width;
        }
        
        @Override
        public int hashCode() {
            return gridCell;
        }
    }
    
    public SpatialHashing() {
        this.width = ((int)max - (int)min) / cellSize;
        this.conversionFactor = 1 / cellSize;
        
        // Initialize main storage
        this.grid = new HashMap<>();
        
        // Initialize reusable objects
        this.reusableSet = new HashSet<>();
        this.reusableList = new ArrayList<>(MAX_OBJECTS_PER_CELL);
        this.tempCell = new GridCell();
        this.tempPoint = new Point2D(0, 0);
        this.neighborCells = new GridCell[MAX_NEIGHBORS];
        
        // Initialize neighbor cells array
        for (int i = 0; i < MAX_NEIGHBORS; i++) {
            neighborCells[i] = new GridCell();
        }
    }
    

    //Is this best implemntation?
    public void processCollisions(CollisionCallback<T> callback) {
        // Process each cell
        for (Set<T> objectsInCell : grid.values()) {
            if (objectsInCell == null || objectsInCell.size() < 2) continue;
            
            // Process objects within the same cell
            reusableList.clear();
            reusableList.addAll(objectsInCell);
            
            int size = reusableList.size();
            for (int i = 0; i < size; i++) {
                T obj1 = reusableList.get(i);
                for (int j = i + 1; j < size; j++) {
                    callback.processCollision(obj1, reusableList.get(j));
                }
            }
        }
    }
    
    public void insert(Point2D point, T object) {
        tempCell.update(point, cellSize, conversionFactor, width);
        grid.computeIfAbsent(tempCell.gridCell, k -> new HashSet<>()).add(object);
    }
    
    public void insert(int x, int y, T object) {
        tempCell.update(x, y, width);
        grid.computeIfAbsent(tempCell.gridCell, k -> new HashSet<>()).add(object);
    }
    
    public Set<T> getPotentialCollisions(Point2D point) {
        reusableSet.clear();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                double newX = point.getX() + dx * cellSize;
                double newY = point.getY() + dy * cellSize;
                
                tempCell.update(new Point2D(newX, newY), cellSize, conversionFactor, width);
                Set<T> cellObjects = grid.get(tempCell.gridCell);
                if (cellObjects != null) {
                    reusableSet.addAll(cellObjects);
                }
            }
        }
        
        return new HashSet<>(reusableSet);
    }
    
    public void clear() {
        grid.clear();
    }
}