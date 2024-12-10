package nbody.PhysicsEngine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.geometry.Point2D;

public class CollisionManager {
    float friction;
    float gravity;
    Vector<Integer> Boundaries = new Vector<>(Arrays.asList(0,0,0,0));
    Vector<VerletObject> m_objects;
    private final SpatialHashing<VerletObject> spatialHash;



    public CollisionManager(float f, float g, Vector<VerletObject> objs) {   
        m_objects = objs;
        friction = f;
        gravity = g;
        spatialHash = new SpatialHashing<>();



    }

    public CollisionManager(Vector<VerletObject> objs) {   
        m_objects = objs;
        spatialHash = new SpatialHashing<>();
     
    }






    public void SpatialHashCollision() {
        spatialHash.clear();
        insertObjectsIntoHash();
        spatialHash.processCollisions((object1, object2) -> {
            handleCollision(object1, object2);
        });
    }
    
    private void insertObjectsIntoHash() {
        for (VerletObject obj : m_objects) {
            Point2D pos = obj.getPosition();
            if (pos != null) {
                spatialHash.insert(pos, obj);
            }
        }
    }



public void quadTreeCollision() {
    float response_coef = 0.75f;
    
    QuadTree.QuadTreeObject<VerletObject> helper = new QuadTree.QuadTreeObject<>() {
        @Override
        public Point2D getPosition(VerletObject object) {
            return object.getPosition();
        }
        
        @Override
        public float getRadius(VerletObject object) {
            return object.getRadius();
        }
    };
    
    // Create quadtree
    QuadTree<VerletObject> quadTree = new QuadTree<>(0, new QuadTree.Rectangle(0, 0, 2000, 2000), helper);
    
    // Insert all objects into quadtree
    for (VerletObject obj : m_objects) {
        if (obj.getPosition() != null) {  // Add null check
            quadTree.insert(obj);
        }
    }
    
    // Check collisions
    List<VerletObject> potentialCollisions = new ArrayList<>();
    for (VerletObject object1 : m_objects) {
        if (object1.getPosition() == null) continue;  // Skip if position is null
        
        potentialCollisions.clear();
        quadTree.findPotentialCollisions(object1, potentialCollisions);
        
        for (VerletObject object2 : potentialCollisions) {
            if (object1 == object2) continue;
            
            Point2D position1 = object1.getPosition();
            Point2D position2 = object2.getPosition();
            
            if (position1 == null || position2 == null) continue;  // Skip if either position is null
            
            float radius1 = object1.getRadius();
            float radius2 = object2.getRadius();

            double dx = position1.getX() - position2.getX();
            double dy = position1.getY() - position2.getY();
            double distSquared = dx * dx + dy * dy;
            float minDist = radius1 + radius2;

            if (distSquared < minDist * minDist) {
                double dist = Math.sqrt(distSquared);
                Point2D normal = new Point2D(dx / dist, dy / dist);

                float massRatio1 = radius1 / (radius1 + radius2);
                float massRatio2 = radius2 / (radius1 + radius2);
                float delta = 0.5f * response_coef * ((float) dist - minDist);

                object1.SetPosition(position1.subtract(normal.multiply(massRatio2 * delta)));
                object2.SetPosition(position2.add(normal.multiply(massRatio1 * delta)));
            }
        }
    }
}



    public void BruteForceSolve() {        
        float response_coef = 0.75f;
        int objects_count = m_objects.size();
        
        for(int i = 0; i < objects_count; i++) {
            VerletObject object_1 = m_objects.get(i);
            for(int j = i + 1; j < objects_count; j++) {
                VerletObject object_2 = m_objects.get(j);
                
                Point2D pos1 = object_1.getPosition();
                Point2D pos2 = object_2.getPosition();
                
                double dx = pos1.getX() - pos2.getX();
                double dy = pos1.getY() - pos2.getY();
                double v_squared = dx * dx + dy * dy;
                float min_dist = object_1.getRadius() + object_2.getRadius();

                if(v_squared < min_dist * min_dist) {
                    double dist = Math.sqrt(v_squared);
                    Point2D normal = new Point2D(dx/dist, dy/dist);
                    
                    float mass_ratio_1 = object_1.getRadius() / (object_1.getRadius() + object_2.getRadius());
                    float mass_ratio_2 = object_2.getRadius() / (object_1.getRadius() + object_2.getRadius());
                    float delta = 0.5f * response_coef * ((float)dist - (float)min_dist);
                    
                    object_1.SetPosition(pos1.subtract(normal.multiply(mass_ratio_2 * delta)));
                    object_2.SetPosition(pos2.add(normal.multiply(mass_ratio_1 * delta)));
                }
            }
        }
    }

    
    

    public boolean collide(VerletObject object1, VerletObject object2) {
        if (!(object1 instanceof VerletObject) || !(object2 instanceof VerletObject)) {
            return false;
        }
        
        VerletObject obj1 = (VerletObject) object1;
        VerletObject obj2 = (VerletObject) object2;
        
        Point2D pos1 = obj1.getPosition();
        Point2D pos2 = obj2.getPosition();
        
        if (pos1 == null || pos2 == null) return false;
        
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double distSquared = dx * dx + dy * dy;
        
        float minDist = obj1.getRadius() + obj2.getRadius();
        
        return distSquared < minDist * minDist;
    }
    private void handleCollision(VerletObject object1, VerletObject object2) {
        float response_coef = 0.75f;
        Point2D position1 = object1.getPosition();
        Point2D position2 = object2.getPosition();
        float radius1 = object1.getRadius();
        float radius2 = object2.getRadius();

        double dx = position1.getX() - position2.getX();
        double dy = position1.getY() - position2.getY();
        double distSquared = dx * dx + dy * dy;
        float minDist = radius1 + radius2;

        if (distSquared < minDist * minDist) {
            double dist = Math.sqrt(distSquared);
            Point2D normal = new Point2D(dx / dist, dy / dist);

            float massRatio1 = radius1 / (radius1 + radius2);
            float massRatio2 = radius2 / (radius1 + radius2);
            float delta = 0.5f * response_coef * ((float) dist - minDist);

            object1.SetPosition(position1.subtract(normal.multiply(massRatio2 * delta)));
            object2.SetPosition(position2.add(normal.multiply(massRatio1 * delta)));
        }
    }


    private synchronized void handleCollision(VerletObject object1, VerletObject object2, float response_coef) {
        Point2D position1 = object1.getPosition();
        Point2D position2 = object2.getPosition();
        
        if (position1 == null || position2 == null) return;
        
        float radius1 = object1.getRadius();
        float radius2 = object2.getRadius();

        double dx = position1.getX() - position2.getX();
        double dy = position1.getY() - position2.getY();
        double distSquared = dx * dx + dy * dy;
        float minDist = radius1 + radius2;

        if (distSquared < minDist * minDist) {
            double dist = Math.sqrt(distSquared);
            Point2D normal = new Point2D(dx / dist, dy / dist);

            float massRatio1 = radius1 / (radius1 + radius2);
            float massRatio2 = radius2 / (radius1 + radius2);
            float delta = 0.5f * response_coef * ((float) dist - minDist);

            object1.SetPosition(position1.subtract(normal.multiply(massRatio2 * delta)));
            object2.SetPosition(position2.add(normal.multiply(massRatio1 * delta)));
        }
    }


}

