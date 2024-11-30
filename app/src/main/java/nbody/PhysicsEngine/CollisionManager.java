package nbody.PhysicsEngine;

import java.util.Arrays;
import java.util.Vector;
import java.lang.Math; 
import javafx.geometry.Point2D;

public class CollisionManager {
    float friciton;
    float gravity;
    Vector<Integer> Boundaries = new Vector<>(Arrays.asList(0,0,0,0));
    //Gonna Usee Vector instead of arraylist cuz its appartnly safer for multi threading 
    Vector<VerletObject> m_objects;



    public CollisionManager(float f, float g, Vector<VerletObject> objs)
    {   
        m_objects = objs;
        friciton = f;
        gravity = g;
    }

    public CollisionManager(Vector<VerletObject> objs)
    {   
        m_objects = objs;
    }


    public void BruteForceSolve()
    {        
        float response_coef = 0.75f;
        int objects_count = m_objects.size();

            for(int i = 0; i < objects_count; i++){
                VerletObject object_1 = m_objects.get(i);
                    for(int j = i + 1; j < objects_count; j++){
                        VerletObject object_2 = m_objects.get(j);
                        
                        //Distance 
                        Point2D v  =  object_1.getPosition() - object_2.getPosition();

                        float v_squared = v.x * v.x + v.y * v.y;

                        float min_dist = object_1.getRadius() + object_2.getRadius();

                            if(v_squared < min_dist * min_dist){

                                float dist = sqrt(v_squared);
                                Point2D normal = v / dist;
                                
                                float object_1_mass_ratio = object_1.getRadius() / (object_1.getRadius() + object_2.getRadius());
                                float object_2_mass_ratio = object_2.getRadius() / (object_1.getRadius() + object_2.getRadius());

                            }
                }
            }
    }
    

}
