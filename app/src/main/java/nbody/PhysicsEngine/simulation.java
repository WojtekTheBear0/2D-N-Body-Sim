package nbody.PhysicsEngine;
import java.util.Vector;

import javafx.geometry.Point2D;

public class simulation {
    private CollisionManager collisionManager;
    private VerletSystemManger systemManager;
    private Point2D grav;
    float box_right;
    float box_left;
    float box_top;
    float box_bottom;
    int m_sub_steps = 1;
    float m_time = 0.0f;
    float m_frame_dt = 0.0f;
    public final int NUMBER_OF_STREAMS = 9; // Default number of streams
    private boolean isSpawning = true; // Add this field
    
    // Add these methods
    public void pauseSpawning() {
        isSpawning = false;
    }
    
    public void resumeSpawning() {
        isSpawning = true;
    }

    public simulation(float width, float height) {
        // Initialize with multiple streams
        systemManager = new VerletSystemManger(10000, 0.00005, NUMBER_OF_STREAMS);
        Vector<VerletObject> objectVector = new Vector<>(systemManager.getObjects());
        collisionManager = new CollisionManager(objectVector);
        grav = new Point2D(0, 550.81);  // Default gravity
        
        // Set up default spawn positions for streams
        setupDefaultStreams(width);
    }

    private void setupDefaultStreams(float width) {
        float streamSpacing = width / (NUMBER_OF_STREAMS + 1);
        for (int i = 0; i < NUMBER_OF_STREAMS; i++) {
            systemManager.setSpawnPosition(i, new Point2D(streamSpacing * (i + 1), 100));
        }
    }
    
    public void setBoundaries(float box_t, float box_b, float box_l, float box_r) {
        this.box_top = box_t;
        this.box_bottom = box_b;
        this.box_left = box_l;
        this.box_right = box_r;
    }

    public void AddStream(){
                // Spawn objects from all streams
      if (isSpawning) {
        systemManager.StreamSpawnObject(2.5f, GetStepDt());
    }
    
    }
    public void update() {
        m_time += GetStepDt();
        float step_dt = GetStepDt();
        

        
        for (int i = m_sub_steps; i > 0; i--) {
            collisionManager.m_objects = new Vector<>(systemManager.getObjects());
            collisionManager.quadTreeCollision();
            ApplyGrav();
            applyConstraint(step_dt);
            updateObjects(step_dt);
        }
    }
    
    public void updateObjects(float dt) {
        for (VerletObject obj : systemManager.getObjects()) {
            obj.update(dt);
        }
    }
    
    public void SpatialHashingCollision() {
        collisionManager.SpatialHashCollision();
    }
    
    public void applyConstraint(float dt) {
        for (VerletObject obj : systemManager.getObjects()) {
            float currentX = obj.getPositionX();
            float currentY = obj.getPositionY();
            
            if (currentX > box_right) {
                obj.setPositionX(box_right);
            } 
            else if (currentX < box_left) {
                obj.setPositionX(box_left);
            }
            
            if (currentY > box_bottom) {
                obj.setPositionY(box_bottom);
            } 
            else if (currentY < box_top) {
                obj.setPositionY(box_top);
            }
        }
    }
    
    // Updated to work with new multi-stream system
    public void setStreamPosition(int streamIndex, Point2D spawnPos) {
        systemManager.setSpawnPosition(streamIndex, spawnPos);
    }
    
    public void ApplyGrav() {
        for (VerletObject obj : systemManager.getObjects()) {
            obj.AddAcceleration(grav);
        }
    }
    
    public void setSimuationUpdateRate(float rate) {
        m_frame_dt = 1.0f / rate;
    }
    
    public void setSubStepsCount(int sub_steps) {
        m_sub_steps = sub_steps;
    }
    
    public VerletSystemManger getSystemManager() {
        return systemManager;
    }
    
    public int getObjectCount() {
        return systemManager.getObjectCount();
    }
    
    public void addObject(VerletObject object) {
        systemManager.addObject(object);
        collisionManager.m_objects = new Vector<>(systemManager.getObjects());
    }
    
    public float GetStepDt() {
        return m_frame_dt / (float)m_sub_steps;
    }


    public void clearObjects() {
        systemManager.getObjects().clear();
        collisionManager.m_objects.clear();
    }

}