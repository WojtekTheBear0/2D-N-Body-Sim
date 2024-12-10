package nbody.PhysicsEngine;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class VerletSystemManger {
    private List<VerletObject> objects;
    private List<Long> lastSpawnTimes;
    private List<Point2D> spawnPositions;
    private final int maxObjectCount;
    private final double spawnDelay;
    private final int numberOfStreams;
    
    public VerletSystemManger(int maxObjectCount, double spawnDelaySeconds, int streams) {
        objects = new ArrayList<>();
        this.maxObjectCount = maxObjectCount;
        this.spawnDelay = spawnDelaySeconds * 1_000_000_000; // Convert to nanoseconds
        this.numberOfStreams = streams;
        
        // Initialize spawn positions and timers for each stream
        lastSpawnTimes = new ArrayList<>();
        spawnPositions = new ArrayList<>();
        
        // Set up initial spawn positions and times for each stream
        for (int i = 0; i < streams; i++) {
            lastSpawnTimes.add(System.nanoTime());
            // Distribute spawn points horizontally with some spacing
            spawnPositions.add(new Point2D(100 + (i * 200), 100));
        }
    }
    
    //Default initializer 
    public VerletSystemManger() {
        objects = new ArrayList<>();
        this.spawnDelay = 0;
        this.maxObjectCount = 0;
        this.numberOfStreams = 1;
        lastSpawnTimes = new ArrayList<>();
        spawnPositions = new ArrayList<>();
        lastSpawnTimes.add(System.nanoTime());
        spawnPositions.add(new Point2D(100, 100));
    }

    public VerletSystemManger addObject(VerletObject object, Point2D Pos, float mass, float radius) {
        object.SetPosition(Pos);
        object.SetMass(mass);
        object.SetRadius(radius);
        objects.add(object);
        return this;
    }

    public VerletSystemManger addObject(VerletObject object) {
        objects.add(object);
        return this;
    }

    public List<VerletObject> getObjects() {
        return objects;
    }

    private boolean canSpawn(long currentTime, int streamIndex) {
        return (currentTime - lastSpawnTimes.get(streamIndex)) >= spawnDelay;
    }

    public boolean StreamSpawnObject(float radius, float dt) {
        boolean spawned = false;
        long currentTime = System.nanoTime();
        Point2D initial_speed = new Point2D(500, 20);

        if (getObjectCount() < maxObjectCount) {
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

    public void clear() {
        objects.clear();
    }
    public void clearObjects(){
        objects.clear();
    }
    public int getObjectCount() {
        return objects.size();
    }
    public int getNumberOfStreams() {
        return numberOfStreams;
    }

}