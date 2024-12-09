package nbody.PhysicsEngine;

import java.util.Vector;

import javafx.geometry.Point2D;

public class simulation {
    private CollisionManager collisionManager;
    private VerletSystemManger systemManager;  // Add system manager
    private Point2D grav;  // Add gravity vector
    float box_right;
    float box_left;
    float box_top;
    float box_bottom;
    int m_sub_steps = 1;
    float m_time = 0.0f;
    float m_frame_dt = 0.0f;
    


    public simulation(float box_t,float box_l) {
        systemManager = new VerletSystemManger(5000,0.05);
        Vector<VerletObject> objectVector = new Vector<>(systemManager.getObjects());
        collisionManager = new CollisionManager(objectVector);
        grav = new Point2D(0, 150.81);  // Default gravity

        box_top = 10;
        box_l = 10;
        box_bottom = 700;
        box_right = 700;
    }
    public void update()
    {
        m_time += GetStepDt();

        float step_dt = GetStepDt();
        for (int i = m_sub_steps; i > 0; i--) {
            //This is prob not the best way to do this lol
            collisionManager.m_objects = new Vector<>(systemManager.getObjects());
            collisionManager.quadTreeCollision();
            ApplyGrav();
            applyConstraint(step_dt);
            updateObjects(step_dt);
        }
    }


    public void updateObjects(float dt){
        for (VerletObject obj : systemManager.getObjects()) {
            obj.update(dt);
        }
    }

    public void SpatialHashingCollision(){
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


    public void addStream(Point2D spawnpos,float radius){
        systemManager.StreamSpawnObject(spawnpos, radius);

    }
    public void ApplyGrav(){
        for (VerletObject obj : systemManager.getObjects()) {
            obj.AddAcceleration(grav);
        }
    }
    public void setSimuationUpdateRate(float rate){
        m_frame_dt = 1.0f / rate;
    }

    public void setSubStepsCount(int sub_steps){
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
        // Update collision manager's object list after adding new object
        collisionManager.m_objects = new Vector<>(systemManager.getObjects());
    }

    public float GetStepDt(){
        return m_frame_dt / (float)m_sub_steps;
    }
}
