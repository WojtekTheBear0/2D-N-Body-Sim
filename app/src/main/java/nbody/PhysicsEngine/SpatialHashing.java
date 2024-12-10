package nbody.PhysicsEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import javafx.geometry.Point2D;
import javafx.util.Pair;

public class SpatialHashing<T> {
    private static final int DEFAULT_CELL_SIZE = 25; // Increased from 8 for better performance
    private final int width = 1000; // Match your quadtree dimensions
    private final int height = 1000;
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private final ConcurrentMap<Integer, Set<T>> grid;
    
    private final ExecutorService executor; // Fixed thread pool

    public interface CollisionCallback<T> {
        void processCollision(T object1, T object2);
    }

    /**
     * Default thread count constructor.
     */
    public SpatialHashing() {
        this(DEFAULT_THREAD_COUNT);
    }

    /**
     * Constructor allowing specification of thread count.
     * @param threadCount The number of threads in the pool.
     */
    public SpatialHashing(int threadCount) {
        this.cellSize = DEFAULT_CELL_SIZE;
        this.gridWidth = width / cellSize + 1;
        this.gridHeight = height / cellSize + 1;
        this.grid = new ConcurrentHashMap<>(gridWidth * gridHeight);
        
        // Initialize a fixed thread pool with the specified number of threads
        this.executor = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT);
    }

    private static final int DEFAULT_THREAD_COUNT = 6; // Example default

    private int hashCoords(int x, int y) {
        return x + y * gridWidth;
    }

    public void clear() {
        grid.clear();
    }

    public void insert(Point2D pos, T object) {
        if (pos == null) return;

        int gridX = (int) (pos.getX() / cellSize);
        int gridY = (int) (pos.getY() / cellSize);

        // Add to primary cell
        insertIntoCell(gridX, gridY, object);

        // Add to neighboring cells if near borders
        float localX = (float) (pos.getX() - (gridX * cellSize));
        float localY = (float) (pos.getY() - (gridY * cellSize));
        float buffer = 5.0f;

        if (localX < buffer) {
            insertIntoCell(gridX - 1, gridY, object);
        } else if (localX > cellSize - buffer) {
            insertIntoCell(gridX + 1, gridY, object);
        }

        if (localY < buffer) {
            insertIntoCell(gridX, gridY - 1, object);
        } else if (localY > cellSize - buffer) {
            insertIntoCell(gridX, gridY + 1, object);
        }
    }

    private void insertIntoCell(int gridX, int gridY, T object) {
        if (gridX < 0 || gridY < 0 || gridX >= gridWidth || gridY >= gridHeight) {
            return;
        }
        int hash = hashCoords(gridX, gridY);
        grid.computeIfAbsent(hash, k -> ConcurrentHashMap.newKeySet()).add(object);
    }

    public void insertAll(List<Pair<Point2D, T>> objects) {
        objects.parallelStream().forEach(pair -> {
            insert(pair.getKey(), pair.getValue());
        });
    }

    /**
     * Processes collisions using a fixed number of threads.
     * @param callback The collision callback to handle collision logic.
     */
    public void processCollisions(CollisionCallback<T> callback) {
        List<Future<?>> futures = new ArrayList<>();

        for (Set<T> cellObjects : grid.values()) {
            if (cellObjects == null || cellObjects.size() < 2) continue;

            // Submit a collision processing task for each cell
            Future<?> future = executor.submit(() -> {
                List<T> objects = new ArrayList<>(cellObjects);
                int size = objects.size();

                for (int i = 0; i < size; i++) {
                    T obj1 = objects.get(i);
                    for (int j = i + 1; j < size; j++) {
                        callback.processCollision(obj1, objects.get(j));
                    }
                }
            });
            futures.add(future);
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(); // This will block until the individual task is complete
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                // Optionally handle the interruption
            } catch (ExecutionException e) {
                // Handle exceptions thrown by the task
                e.printStackTrace();
            }
        }
    }

    /**
     * Shuts down the executor service gracefully.
     * Should be called when the SpatialHashing instance is no longer needed.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for existing tasks to terminate
                executor.shutdownNow(); // Force shutdown if not terminated
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow(); // Re-cancel if current thread is interrupted
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }
    }
}
