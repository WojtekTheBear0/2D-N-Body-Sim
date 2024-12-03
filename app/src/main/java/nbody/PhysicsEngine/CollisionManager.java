package nbody.PhysicsEngine;
import java.util.Arrays;
import java.util.Vector;

import javafx.geometry.Point2D;

public class CollisionManager {
    float friction;
    float gravity;
    Vector<Integer> Boundaries = new Vector<>(Arrays.asList(0,0,0,0));
    Vector<VerletObject> m_objects;

    public CollisionManager(float f, float g, Vector<VerletObject> objs) {   
        m_objects = objs;
        friction = f;
        gravity = g;
    }

    public CollisionManager(Vector<VerletObject> objs) {   
        m_objects = objs;
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
}