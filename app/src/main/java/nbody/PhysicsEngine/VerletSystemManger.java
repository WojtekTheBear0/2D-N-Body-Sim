package nbody.PhysicsEngine;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class VerletSystemManger {
    private List<VerletObject> objects;
    private List<Long> lastSpawnTimes;
    private List<Point2D> spawnPositions;
    private final int maxObjectCount;
    private double spawnDelay;
    private double spawnDelaySeconds;
    private int numberOfStreams;  // Changed to non-final
    public boolean StreamActive = true;
    
    public VerletSystemManger(int maxObjectCount, double spawnDelaySeconds, int streams) {
        objects = new ArrayList<>();
        this.maxObjectCount = maxObjectCount;
        this.spawnDelaySeconds = spawnDelaySeconds;
        this.spawnDelay = spawnDelaySeconds * 1_000_000_000; // Convert to nanoseconds
        this.numberOfStreams = streams;
        
        // Initialize spawn positions and timers for each stream
        lastSpawnTimes = new ArrayList<>();
        spawnPositions = new ArrayList<>();
        
        // Set up initial spawn positions and times for each stream
        for (int i = 0; i < streams; i++) {
            lastSpawnTimes.add(System.nanoTime());
            // Distribute spawn points horizontally with some spacing
            spawnPositions.add(new Point2D(25, 100 + (i * 200)));
        }
    }
    
    // Default initializer 
    public VerletSystemManger() {
        objects = new ArrayList<>();
        this.spawnDelay = 0;
        this.spawnDelaySeconds = 0;
        this.maxObjectCount = Integer.MAX_VALUE;
        lastSpawnTimes = new ArrayList<>();
        spawnPositions = new ArrayList<>();
        lastSpawnTimes.add(System.nanoTime());
        spawnPositions.add(new Point2D(0, 0));
    }

    // Method to dynamically change number of streams
    public void setNumberOfStreams(int newNumberOfStreams) {
        if (newNumberOfStreams > 0) {
            // If increasing number of streams
            if (newNumberOfStreams > this.numberOfStreams) {
                // Add new streams
                for (int i = this.numberOfStreams; i < newNumberOfStreams; i++) {
                    lastSpawnTimes.add(System.nanoTime());
                    // Calculate new spawn position based on existing pattern
                    double newY = (i * 15); // 25-pixel intervals instead of 200
                    spawnPositions.add(new Point2D(25, newY));
                }
            } 
            // If decreasing number of streams, remove excess
            else if (newNumberOfStreams < this.numberOfStreams) {
                // Remove excess spawn times and positions
                while (lastSpawnTimes.size() > newNumberOfStreams) {
                    lastSpawnTimes.remove(lastSpawnTimes.size() - 1);
                    spawnPositions.remove(spawnPositions.size() - 1);
                }
            }
    
            // Update the number of streams
            this.numberOfStreams = newNumberOfStreams;
        } else {
            throw new IllegalArgumentException("Number of streams must be positive");
        }
    }
    // Method to add an object with full configuration
    public VerletSystemManger addObject(VerletObject object, Point2D pos, float mass, float radius) {
        object.SetPosition(pos);
        object.SetMass(mass);
        object.SetRadius(radius);
        objects.add(object);
        return this;
    }

    // Method to add an object with minimal configuration
    public VerletSystemManger addObject(VerletObject object) {
        objects.add(object);
        return this;
    }

    // Get all objects in the system
    public List<VerletObject> getObjects() {
        return objects;
    }

    // Check if an object can be spawned from a specific stream
    private boolean canSpawn(long currentTime, int streamIndex) {
        return (currentTime - lastSpawnTimes.get(streamIndex)) >= spawnDelay;
    }

    // Spawn objects across multiple streams
    public boolean StreamSpawnObject(float radius, float dt) {
        boolean spawned = false;
        long currentTime = System.nanoTime();
        Point2D initial_speed = new Point2D(500, 20);
        
        if (getObjectCount() < maxObjectCount && StreamActive) {
            // Try to spawn from each stream
            for (int i = 0; i < numberOfStreams; i++) {
                if (canSpawn(currentTime, i)) {
                    lastSpawnTimes.set(i, currentTime);
                    VerletObject object = new VerletObject(spawnPositions.get(i), radius, 5.0f);
                    object.SetVelocity(initial_speed, dt);
                    addObject(object);
                    spawned = true;
                }
            }
        }
        return spawned;
    }

    // Allow customization of spawn positions
    public void setSpawnPosition(int streamIndex, Point2D position) {
        if (streamIndex >= 0 && streamIndex < numberOfStreams) {
            spawnPositions.set(streamIndex, position);
        }
    }

    // Dynamically change spawn rate
    public void setSpawnRate(double objectsPerSecond) {
        // Clamp rate between a minimum and maximum to prevent extreme values
        double clampedRate = Math.max(0.1, Math.min(objectsPerSecond, 500.0));
        
        // Calculate spawn delay with protection against division by zero
        this.spawnDelaySeconds = (clampedRate > 0) ? (1.0 / clampedRate) : Double.MAX_VALUE;
        
        // Convert to nanoseconds with overflow protection
        this.spawnDelay = Math.min(
            spawnDelaySeconds * 1_000_000_000, 
            Long.MAX_VALUE
        );
        
        // Efficiently reset last spawn times
        long currentTime = System.nanoTime();
        for (int i = 0; i < numberOfStreams; i++) {
            lastSpawnTimes.set(i, currentTime);
        }
    }
    
    
    

    // Get current spawn rate
    public double getSpawnRate() {
        return spawnDelaySeconds;
    }

    // Clear all objects
    public void clear() {
        objects.clear();
    }

    // Alternative method for clearing objects
    public void clearObjects() {
        objects.clear();
    }

    // Get current object count
    public int getObjectCount() {
        return objects.size();
    }

    // Get number of streams
    public int getNumberOfStreams() {
        return numberOfStreams;
    }

    // Set stream activity
    public void setStreamActive(boolean active) {
        this.StreamActive = active;
    }

    // Check if streaming is active
    public boolean isStreamActive() {
        return StreamActive;
    }
}