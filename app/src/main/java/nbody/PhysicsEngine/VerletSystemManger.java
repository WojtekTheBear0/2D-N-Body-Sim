package nbody.PhysicsEngine;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class VerletSystemManger {
        private List<VerletObject> objects;
        private long lastSpawnTime = System.nanoTime();
        private final int maxObjectCount;
        private final double spawnDelay;



        //
        public VerletSystemManger(int maxObjectCount, double spawnDelaySeconds) {
            objects = new ArrayList<>();
            this.maxObjectCount = maxObjectCount;
            this.spawnDelay = spawnDelaySeconds * 1_000_000_000; // Convert to nanoseconds
        }
    
    //Default initializer 
    public VerletSystemManger() {
        objects = new ArrayList<>();
        this.spawnDelay = 0; // Convert to nanoseconds
        this.maxObjectCount = 0;

    }


    public VerletSystemManger addObject(VerletObject object, Point2D Pos,float mass, float radius) {
        object.SetPosition(Pos);
        object.SetMass(mass);
        object.SetRadius(radius);
        objects.add(object);
        return this;  // Allow method chaining
    }

    public VerletSystemManger addObject(VerletObject object) {
        objects.add(object);
        return this;  // Allow method chaining
    }



    public List<VerletObject> getObjects() {
        return objects;
    }


    private boolean canSpawn(long currentTime) {
        return (currentTime - lastSpawnTime) >= spawnDelay;
    }



    public boolean StreamSpawnObject(Point2D spawnpos, float radius){
        //Get time  
        long currentTime = System.nanoTime();
        Point2D initial_speed = new Point2D(100,20); 

        if( getObjectCount() < maxObjectCount &&  canSpawn(currentTime) ) {
            lastSpawnTime = currentTime;
            VerletObject object = new VerletObject(spawnpos, radius,5.0f);
            object.SetVelocity(initial_speed,(float)0.0016);


            
            addObject(object);
            // System.out.println("Object spawned. Count: " + getObjectCount()); // Debug

            return true;
        }

        return false;
    }

    public void clear() {
        objects.clear();
    }

    public int getObjectCount() {
        return objects.size();
    }
}
