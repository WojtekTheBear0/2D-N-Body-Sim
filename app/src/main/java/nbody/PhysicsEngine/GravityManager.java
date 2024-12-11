package nbody.PhysicsEngine;

import java.util.List;

import javafx.geometry.Point2D;

public class GravityManager {

    private static final double G = 6.67343e-11;
    private static final double thetaThreshold = .5;

    // Method to compute gravitational forces using brute force (pairwise)
    public static void computeForcesBruteForce(List<VerletObject> objects) {
        // Loop through all the objects and calculate the accelerations on them
        // Do j = i + 1 to prevent redundant additions ((n-1)! coparisions per outer loop)
        for (int i = 0; i < objects.size(); i++) {
            VerletObject obj1 = objects.get(i);

            for (int j = i + 1; j < objects.size(); j++) {
                VerletObject obj2 = objects.get(j);

                // Calculate force and direction
                Point2D force = getVectorForce(obj1, obj2);

                // Apply the forces to both objects (Newton's Third Law)
                obj1.AddAcceleration(force.multiply(1 / obj1.getMass()));
                obj2.AddAcceleration(force.multiply(-1 / obj2.getMass()));
            }
        }
    }


    // Gets force as a 2D vector
    public static Point2D getVectorForce(VerletObject obj1, VerletObject obj2) {
        Point2D pos1 = obj1.getPosition();
        Point2D pos2 = obj2.getPosition();

        double distance = pos1.distance(pos2);

        if (distance == 0) {
            return Point2D.ZERO; // Avoid division by zero
        }

        double forceMagnitude = G * obj1.getMass() * obj2.getMass() / (distance * distance);
        Point2D direction = pos2.subtract(pos1).normalize();

        return direction.multiply(forceMagnitude);
    }


    // Get magnitude of the force
    public static double getMagnitudeForce(VerletObject obj1, VerletObject obj2) {
        double distance = obj1.getPosition().distance(obj2.getPosition());
        return G * obj1.getMass() * obj2.getMass() / (Math.pow(distance, 2));
    }



    /* 
    // Method to compute gravitational forces using QuadTree 
    public static void computeForcesWithQuadTree(List<VerletObject> objects) {
        // Create a QuadTree based on the bounds of the simulation 
        QuadTree.QuadTreeObject<VerletObject> helper = new QuadTree.QuadTreeObject<>() {
            @Override
            public Point2D getPosition(VerletObject object) {
                return object.getPosition();
            }
            
            @Override
            public float getRadius(VerletObject object) {
                return object.getRadius();
            }

            @Override
            public double getMass(VerletObject object) {
                return object.getMass();
            }
        };
        
        // Create the QuadTree with the appropriate bounds
        QuadTree<VerletObject> quadTree = new QuadTree<>(0, new QuadTree.Rectangle(0, 0, 2000, 2000), helper);

        // Insert all objects into the QuadTree
        for (VerletObject obj : objects) {
            if (obj.getPosition() != null) {  // Null check to avoid inserting invalid objects
                Point2D force = quadTree.calculateForceUsingTree(obj, thetaThreshold);
                obj.AddAcceleration(force.multiply(1 / obj.getMass()));
            }
        }
    }
    */
}
